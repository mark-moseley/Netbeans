/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.python.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.python.editor.elements.IndexedElement;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.netbeans.modules.python.editor.scopes.SymInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.argumentsType;
import org.python.antlr.ast.exprType;
import org.python.antlr.ast.stmtType;

/**
 * Utility functions for dealing with the Jython AST
 *
 * @author Tor Norbye
 */
public class PythonAstUtils {
    private PythonAstUtils() {
        // This is just a utility class, no instances expected so private constructor
    }

    public static int getAstOffset(CompilationInfo info, int lexOffset) {
        ParserResult result = info.getEmbeddedResult(PythonTokenId.PYTHON_MIME_TYPE, 0);
        if (result != null) {
            TranslatedSource ts = result.getTranslatedSource();
            if (ts != null) {
                return ts.getAstOffset(lexOffset);
            }
        }

        return lexOffset;
    }

    public static OffsetRange getAstOffsets(CompilationInfo info, OffsetRange lexicalRange) {
        ParserResult result = info.getEmbeddedResult(PythonTokenId.PYTHON_MIME_TYPE, 0);
        if (result != null) {
            TranslatedSource ts = result.getTranslatedSource();
            if (ts != null) {
                int rangeStart = lexicalRange.getStart();
                int start = ts.getAstOffset(rangeStart);
                if (start == rangeStart) {
                    return lexicalRange;
                } else if (start == -1) {
                    return OffsetRange.NONE;
                } else {
                    // Assumes the translated range maintains size
                    return new OffsetRange(start, start + lexicalRange.getLength());
                }
            }
        }
        return lexicalRange;
    }

    public static PythonTree getRoot(CompilationInfo info) {
        return getRoot(info, PythonTokenId.PYTHON_MIME_TYPE);
    }

    public static PythonParserResult getParseResult(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(PythonTokenId.PYTHON_MIME_TYPE, 0);

        if (result == null) {
            return null;
        } else {
            return ((PythonParserResult)result);
        }
    }

    public static PythonTree getRoot(CompilationInfo info, String mimeType) {
        ParserResult result = info.getEmbeddedResult(mimeType, 0);

        if (result == null) {
            return null;
        }

        return getRoot(result);
    }

    public static PythonTree getRoot(ParserResult r) {
        assert r instanceof PythonParserResult;

        PythonParserResult result = (PythonParserResult)r;

        return result.getRoot();
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getNameRange(CompilationInfo info, PythonTree node) {
//        final int type = node.getType();
//        switch (type) {
//        case Token.FUNCTION: {
//            if (node.hasChildren()) {
//                for (PythonTree child = node.getFirstChild(); child != null; child = child.getNext()) {
//                    if (child.getType() == Token.FUNCNAME) {
//                        return getNameRange(child);
//                    }
//                }
//            }
//
//            return getRange(node);
//        }
//        case Token.NAME:
//        case Token.BINDNAME:
//        case Token.FUNCNAME:
//        case Token.PARAMETER:
//        case Token.OBJLITNAME:
//            int start = node.getSourceStart();
//            String name = node.getString();
//            return new OffsetRange(start, start+name.length());
//        case Token.CALL:
//            PythonTree namePythonTree = findCallNamePythonTree(node);
//            if (namePythonTree != null) {
//                return getNameRange(namePythonTree);
//            }
//        }

        // XXX Is there a faster way to determine if it's a function,
        // e.g. some kind of "kind" or "id" or "type" enum attribute on the tree node
        if (node instanceof FunctionDef) {
            FunctionDef def = (FunctionDef)node;
            //node.getType();

            // HACK: There's no separate node for the name offset itself, so I need
            // to figure it out. For now assume that it's exactly 4 characters away
            // from the beginning of "def" - def plus space. If there are multiple spaces
            // this won't work. I ought to look in the document and ensure that the character
            // there in fact is the start of the name, and if not, search forwards for it.
            int DELTA = 4; // HACK:
            int start = def.getCharStartIndex() + DELTA;
            int end = start + def.name.length();

            // TODO - look up offset

            return new OffsetRange(start, end);
        } else if (node instanceof ClassDef) {
            ClassDef def = (ClassDef)node;
            //node.getType();

            // HACK: There's no separate node for the name offset itself, so I need
            // to figure it out. For now assume that it's exactly 6 characters away
            // from the beginning of "class" - class plus space. If there are multiple spaces
            // this won't work. I ought to look in the document and ensure that the character
            // there in fact is the start of the name, and if not, search forwards for it.
            int DELTA = 6; // HACK:
            int start = def.getCharStartIndex() + DELTA;
            int end = start + def.name.length();

            // TODO - look up offset

            return new OffsetRange(start, end);
        } else if (node instanceof Attribute) {
            Attribute attr = (Attribute)node;
            return getNameRange(info, attr.value);
        } else if (node instanceof Call) {
            Call call = (Call)node;
            if (call.func instanceof Name) {
                return getNameRange(info, call.func);
            } else if (call.func instanceof Attribute) {
                // The call name is in the value part of the name.value part
                Attribute attr = (Attribute)call.func;
                int start = attr.value.getCharStopIndex() + 1; // +1: Skip .
                String name = attr.attr;
                return new OffsetRange(start, start + name.length());
            } else {
                String name = getCallName(call);
                if (name != null) {
                    int start = call.getCharStartIndex();
                    return new OffsetRange(start, start + name.length());
                }
            }
        }

        return getRange(node);
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getRange(PythonTree node) {
        final int start = node.getCharStartIndex();
        final int end = node.getCharStopIndex();

//        assert end >= start : "Invalid offsets for " + node + ": start=" + start + " and end=" + end;
        if (end < start) {
            Logger logger = Logger.getLogger(PythonAstUtils.class.getName());
            logger.log(Level.WARNING, "Invalid offsets for " + node + ": start=" + start + " and end=" + end);
            return new OffsetRange(start, start);
        }

        return new OffsetRange(start, end);
    }

    public static boolean isNameNode(PythonTree node) {
        if (node instanceof Name) {
            return true;
        }

        return false;
    }

    /** Compute the module/class name for the given node path */
    public static String getFqnName(AstPath path) {
        StringBuilder sb = new StringBuilder();

        Iterator<PythonTree> it = path.rootToLeaf();

        while (it.hasNext()) {
            PythonTree node = it.next();

            if (node instanceof ClassDef) {
                if (sb.length() > 0) {
                    sb.append('.'); // NOI18N
                }
                ClassDef cls = (ClassDef)node;
                sb.append(cls.name);
            }
        }

        return sb.toString();
    }

    /** Return the node for the local scope containing the given node */
    public static PythonTree getLocalScope(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof FunctionDef) {
                return node;
            }
        }

        return path.root();
    }

    public static PythonTree getClassScope(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return node;
            }
        }

        return path.root();
    }

    public static ClassDef getClassDef(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return (ClassDef)node;
            }
        }

        return null;
    }

    public static boolean isClassMethod(AstPath path, FunctionDef def) {
        // Check to see if (a) the function is inside a class, and (b) it's
        // not nested in a function
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return true;
            }
            // Nested method private to this one?
            if (node instanceof FunctionDef && node != def) {
                return false;
            }
        }

        return false;
    }

    public static FunctionDef getFuncDef(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof FunctionDef) {
                return (FunctionDef)node;
            }
        }

        return null;
    }

    /**
     * Return true iff this call looks like a "getter". If we're not sure,
     * return the default value passed into this method, unknownDefault. 
     */
    public static boolean isGetter(Call call, boolean unknownDefault) {
        String name = PythonAstUtils.getCallName(call);
        if (name == null) {
            return unknownDefault;
        }

        return name.startsWith("get") || name.startsWith("_get"); // NOI18N
    }

    public static String getCallName(Call node) {
        exprType func = node.func;

        return getExprName(func);
    }

    public static String getExprName(exprType type) {
        if (type instanceof Attribute) {
            Attribute attr = (Attribute)type;
            return attr.attr;
        } else if (type instanceof Name) {
            return ((Name)type).id;
        } else if (type instanceof Call) {
            Call call = (Call)type;
            return getExprName(call.func);
        //} else if (type instanceof Str) {
        //    return ((Str)type).getText();
        } else {
            return null;
        }
    }

    public static String getName(PythonTree node) {
        if (node instanceof Name) {
            return ((Name)node).id;
        }
        if (node instanceof Attribute) {
            Attribute attrib = (Attribute)node;
            String prefix = getName(attrib.value);
            return (prefix + '.' + attrib.attr);
        }
        NameVisitor visitor = new NameVisitor();
        try {
            Object result = visitor.visit(node);
            if (result instanceof String) {
                return (String)result;
            } else {
                // TODO HANDLE THIS!
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public static List<String> getParameters(FunctionDef def) {
        argumentsType args = def.args;
        List<String> params = new ArrayList<String>();

        NameVisitor visitor = new NameVisitor();

        for (int i = 0; i < args.args.length; i++) {
            try {
                Object result = visitor.visit(args.args[i]);
                if (result instanceof String) {
                    params.add((String)result);
                } else {
                    // TODO HANDLE THIS!
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (args.vararg != null) {
            params.add(args.vararg);
        }
        if (args.kwarg != null) {
            params.add(args.kwarg);
        }

        return params;
    }

    private static Str searchForDocNode(stmtType stmt) {
        if (stmt instanceof Expr) {
            Expr expr = (Expr)stmt;
            if (expr.value instanceof Str) {
                return (Str)expr.value;
            }
        }

        return null;
    }

    public static Str getDocumentationNode(PythonTree node) {
        // DocString processing.
        // See http://www.python.org/dev/peps/pep-0257/

        // For modules, it's the first Str in the document.
        // For classes and methods, it's the first Str in the object.
        // For others, nothing.

        if (node instanceof FunctionDef) {
            // Function
            FunctionDef def = (FunctionDef)node;
            if (def.body != null && def.body.length > 0) {
                return searchForDocNode(def.body[0]);
            }
        } else if (node instanceof ClassDef) {
            // Class
            ClassDef def = (ClassDef)node;
            if (def.body != null && def.body.length > 0) {
                return searchForDocNode(def.body[0]);
            }
        } else if (node instanceof Module) {
            // Module
            Module module = (Module)node;
            if (module.body != null && module.body.length > 0) {
                return searchForDocNode(module.body[0]);
            }
        }
        // TODO: As per http://www.python.org/dev/peps/pep-0257/ I should
        // also look for "additional docstrings" (Str node following a Str node)
        // and Assign str nodes

        return null;
    }

    public static String getStrContent(Str str) {
        String doc = str.getText();

        // Strip quotes
        // and U and/or R for unicode/raw string. U must always preceede r if present.
        if (doc.startsWith("ur") || doc.startsWith("UR") || // NOI18N
                doc.startsWith("Ur") || doc.startsWith("uR")) { // NOI18N
            doc = doc.substring(2);
        } else if (doc.startsWith("r") || doc.startsWith("u") || // NOI18N
                doc.startsWith("R") || doc.startsWith("U")) { // NOI18N
            doc = doc.substring(1);
        }

        if (doc.startsWith("\"\"\"") && doc.endsWith("\"\"\"")) { // NOI18N
            doc = doc.substring(3, doc.length() - 3);
        } else if (doc.startsWith("r\"\"\"") && doc.endsWith("\"\"\"")) { // NOI18N
            doc = doc.substring(4, doc.length() - 3);
        } else if (doc.startsWith("'''") && doc.endsWith("'''")) { // NOI18N
            doc = doc.substring(3, doc.length() - 3);
        } else if (doc.startsWith("r'''") && doc.endsWith("'''")) { // NOI18N
            doc = doc.substring(4, doc.length() - 3);
        } else if (doc.startsWith("\"") && doc.endsWith("\"")) { // NOI18N
            doc = doc.substring(1, doc.length() - 1);
        } else if (doc.startsWith("'") && doc.endsWith("'")) { // NOI18N
            doc = doc.substring(1, doc.length() - 1);
        }

        return doc;
    }

    public static String getDocumentation(PythonTree node) {
        Str str = getDocumentationNode(node);
        if (str != null) {
            return getStrContent(str);
        }

        return null;
    }

    public static PythonTree getForeignNode(final IndexedElement o, CompilationInfo[] compilationInfoRet) {
        FileObject fo = o.getFileObject();

        if (fo == null) {
            return null;
        }

        SourceModel model = SourceModelFactory.getInstance().getModel(fo);
        if (model == null) {
            return null;
        }
        final CompilationInfo[] infoHolder = new CompilationInfo[1];
        try {
            model.runUserActionTask(new CancellableTask<CompilationInfo>() {
                public void cancel() {
                }

                public void run(CompilationInfo info) throws Exception {
                    infoHolder[0] = info;
                }
                //}, true);
            }, false); // XXX REMOVE THIS REMOVE THIS REMOVE THIS!
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        CompilationInfo info = infoHolder[0];
        if (compilationInfoRet != null) {
            compilationInfoRet[0] = info;
        }
        PythonParserResult result = getParseResult(info);
        if (result == null) {
            return null;
        }

        PythonTree root = getRoot(result);
        if (root == null) {
            return null;
        }

        if (o.getKind() == ElementKind.MODULE && root instanceof Module) {
            return root;
        }

        String signature = o.getSignature();

        if (signature == null) {
            return null;
        }

        SymbolTable symbolTable = result.getSymbolTable();
        SymInfo sym = symbolTable.findBySignature(o.getKind(), signature);
        if (sym != null && sym.node != null) {
            // Temporary diagnostic checking
            //assert ((o.getKind() != ElementKind.CONSTRUCTOR && o.getKind() != ElementKind.METHOD) ||
            //        sym.node instanceof FunctionDef);
            //assert o.getKind() != ElementKind.CLASS || sym.node instanceof ClassDef;

            return sym.node;
        }

        // TODO - check args etc.
//        String name = o.getName();
//        boolean lookForFunction = o.getKind() == ElementKind.CONSTRUCTOR || o.getKind() == ElementKind.METHOD;
//        if (lookForFunction) {
//            for (AstElement element : result.getStructure().getElements()) {
//                if (element.getName().equals(name) && element.getSignature().equals(signature)) {
//                        return element.getNode();
//                    }
//                }
//            }
//        }

        ElementKind kind = o.getKind();
        List<PythonStructureItem> items = result.getStructure().getElements();
        if (items != null) {
            return find(items, signature, kind);
        } else {
            return null;
        }
    }

    private static PythonTree find(List<PythonStructureItem> items, String signature, ElementKind kind) {
        for (PythonStructureItem item : items) {
            ElementKind childKind = item.getKind();
            if (childKind == kind &&
                    signature.equals(item.getSignature())) {
                return item.getNode();
            }
            if (childKind == ElementKind.CLASS && signature.indexOf(item.getName()) != -1) {
                @SuppressWarnings("unchecked")
                List<PythonStructureItem> children = (List<PythonStructureItem>)item.getNestedItems();
                if (children != null) {
                    PythonTree result = find(children, signature, kind);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    private static final class NameVisitor extends Visitor {
        @Override
        public Object visitName(Name name) throws Exception {
            return name.id;
        }
    }

    public static Set<OffsetRange> getLocalVarOffsets(CompilationInfo info, int lexOffset) {
        int astOffset = getAstOffset(info, lexOffset);
        if (astOffset != -1) {
            PythonTree root = getRoot(info);
            if (root != null) {
                AstPath path = AstPath.get(root, astOffset);
                if (path != null) {
                    PythonTree closest = path.leaf();
                    PythonTree scope = getLocalScope(path);
                    String name = ((Name)closest).id;

                    return getLocalVarOffsets(info, scope, name);
                }
            }
        }

        return Collections.emptySet();
    }

    public static Set<OffsetRange> getLocalVarOffsets(CompilationInfo info, PythonTree scope, String name) {
        LocalVarVisitor visitor = new LocalVarVisitor(info, name, false, true);
        try {
            visitor.visit(scope);
            return visitor.getOffsets();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptySet();
        }
    }

    public static List<Name> getLocalVarNodes(CompilationInfo info, PythonTree scope, String name) {
        LocalVarVisitor visitor = new LocalVarVisitor(info, name, true, false);
        try {
            visitor.visit(scope);
            return visitor.getVars();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    public static List<Name> getLocalVarAssignNodes(CompilationInfo info, PythonTree scope, String name) {
        LocalVarAssignVisitor visitor = new LocalVarAssignVisitor(info, name, true, false);
        try {
            visitor.visit(scope);
            return visitor.getVars();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    private static class LocalVarVisitor extends Visitor {
        private List<Name> vars = new ArrayList<Name>();
        private Set<OffsetRange> offsets = new HashSet<OffsetRange>();
        private String name;
        private CompilationInfo info;
        private boolean collectNames;
        private boolean collectOffsets;
        private PythonTree parent;

        private LocalVarVisitor(CompilationInfo info, String name, boolean collectNames, boolean collectOffsets) {
            this.info = info;
            this.name = name;
            this.collectNames = collectNames;
            this.collectOffsets = collectOffsets;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            PythonTree oldParent = parent;
            parent = node;
            super.traverse(node);
            parent = oldParent;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (parent instanceof Call && ((Call)parent).func == node) {
                return super.visitName(node);
            }

            if ((name == null && !PythonUtils.isClassName(node.id, false)) ||
                    (name != null && name.equals(node.id))) {
                if (collectOffsets) {
                    OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
                    OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                    if (lexRange != OffsetRange.NONE) {
                        offsets.add(astRange);
                    }
                }
                if (collectNames) {
                    vars.add(node);
                }
            }

            return super.visitName(node);
        }

        public Set<OffsetRange> getOffsets() {
            return offsets;
        }

        public List<Name> getVars() {
            return vars;
        }
    }

    private static class LocalVarAssignVisitor extends Visitor {
        private List<Name> vars = new ArrayList<Name>();
        private Set<OffsetRange> offsets = new HashSet<OffsetRange>();
        private String name;
        private CompilationInfo info;
        private boolean collectNames;
        private boolean collectOffsets;
        private PythonTree parent;

        private LocalVarAssignVisitor(CompilationInfo info, String name, boolean collectNames, boolean collectOffsets) {
            this.info = info;
            this.name = name;
            this.collectNames = collectNames;
            this.collectOffsets = collectOffsets;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (parent instanceof FunctionDef || parent instanceof Assign) {
                if ((name == null && !PythonUtils.isClassName(node.id, false)) ||
                        (name != null && name.equals(node.id))) {
                    if (collectOffsets) {
                        OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
                        OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                        if (lexRange != OffsetRange.NONE) {
                            offsets.add(astRange);
                        }
                    }
                    if (collectNames) {
                        vars.add(node);
                    }
                }
            }

            return super.visitName(node);
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            PythonTree oldParent = parent;
            parent = node;
            super.traverse(node);
            parent = oldParent;
        }

        public Set<OffsetRange> getOffsets() {
            return offsets;
        }

        public List<Name> getVars() {
            return vars;
        }
    }

    /** Collect nodes of the given types (node.nodeId==NodeTypes.x) under the given root */
    public static void addNodesByType(PythonTree root, Class[] nodeClasses, List<PythonTree> result) {
        try {
            new NodeTypeVisitor(result, nodeClasses).visit(root);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static class NodeTypeVisitor extends Visitor {
        private Class[] nodeClasses;
        private List<PythonTree> result;

        NodeTypeVisitor(List<PythonTree> result, Class[] nodeClasses) {
            this.result = result;
            this.nodeClasses = nodeClasses;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            for (int i = 0; i < nodeClasses.length; i++) {
                if (node.getClass() == nodeClasses[i]) {
                    result.add(node);
                    break;
                }
            }

            super.traverse(node);
        }
    }

    public static Name getParentClassFromNode(AstPath path, PythonTree from, String name) {
        ClassDef curClass = (ClassDef)path.getTypedAncestor(ClassDef.class, from);
        if (curClass == null) {
            return null;
        }

        exprType[] baseClasses = curClass.bases;
        if (baseClasses == null) {
            return null; // no inheritance ;
        }
        int ii = 0;
        while (ii < baseClasses.length) {
            if (baseClasses[ii] instanceof Name) {
                Name cur = (Name)baseClasses[ii];
                if (cur.id.equals(name)) {
                    return cur;
                }
            }
            ii++;
        }
        return null;
    }

    /**
     * Look for the caret offset in the parameter list; return the
     * index of the parameter that contains it.
     */
    public static int findArgumentIndex(Call call, int astOffset, AstPath path) {

        // On the name part in the call rather than the args?
        if (astOffset <= call.func.getCharStopIndex()) {
            return -1;
        }

        if (call.args != null) {
            int index = 0;
            for (; index < call.args.length; index++) {
                exprType et = call.args[index];
                if (et.getCharStopIndex() >= astOffset) {
                    return index;
                }
            }
        }

        // TODO what about the other stuff in there -- 
        //call.keywords;
        //call.kwargs;
        //call.starargs;

        return -1;
    }
}
