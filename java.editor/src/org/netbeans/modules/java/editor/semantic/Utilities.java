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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {
    
    private static final Logger LOG = Logger.getLogger(Utilities.class.getName());
    
    @Deprecated
    private static final boolean DEBUG = false;
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }
    
    private static Token<JavaTokenId> findTokenWithText(CompilationInfo info, String text, int start, int end) {
        TokenHierarchy<Void> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language()).subSequence(start, end);
        
        while (ts.moveNext()) {
            Token<JavaTokenId> t = ts.token();
            
            if (text.equals(t.text().toString())) {
                return t;
            }
        }
        
        return null;
    }
    
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, Tree decl, Tree lastLeft, List<? extends Tree> firstRight, String name, CompilationUnitTree cu, SourcePositions positions) {
        int declStart = (int) positions.getStartPosition(cu, decl);
        int start = lastLeft != null ? (int)positions.getEndPosition(cu, lastLeft) : declStart;
        
        if (start == (-1)) {
            start = declStart;
            if (start == (-1)) {
                return null;
            }
        }
        
        int end = (int)positions.getEndPosition(cu, decl);

        for (Tree t : firstRight) {
            if (t == null)
                continue;

            int proposedEnd = (int)positions.getStartPosition(cu, t);

            if (proposedEnd != (-1) && proposedEnd < end)
                end = proposedEnd;
        }

        if (end == (-1)) {
            return null;
        }

        if (start > end) {
            //may happend in case:
            //public static String s() [] {}
            //(meaning: method returning array of Strings)
            //use a conservative start value:
            start = (int) positions.getStartPosition(cu, decl);
        }

        return findTokenWithText(info, name, start, end);
    }
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, MemberSelectTree tree, CompilationUnitTree cu, SourcePositions positions) {
        int start = (int)positions.getStartPosition(cu, tree);
        int endPosition = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || endPosition == (-1))
            return null;

        String member = tree.getIdentifier().toString();

        TokenHierarchy<Void> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());

        if (ts.move(endPosition) == Integer.MAX_VALUE) {
            return null;
        }

        ts.moveNext();

        while (ts.offset() >= start) {
            Token<JavaTokenId> t = ts.token();

            if (member.equals(t.text().toString())) {
                return t;
            }

            if (!ts.movePrevious())
                break;
        }
        return null;
    }
    
    private static final Map<Class, List<Kind>> class2Kind;
    
    static {
        class2Kind = new HashMap<Class, List<Kind>>();
        
        for (Kind k : Kind.values()) {
            Class c = k.asInterface();
            List<Kind> kinds = class2Kind.get(c);
            
            if (kinds == null) {
                class2Kind.put(c, kinds = new ArrayList<Kind>());
            }
            
            kinds.add(k);
        }
    }
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, TreePath decl) {
        if (info.getTreeUtilities().isSynthetic(decl))
            return null;
        
        Tree leaf = decl.getLeaf();
        
        if (class2Kind.get(MethodTree.class).contains(leaf.getKind())) {
            MethodTree method = (MethodTree) leaf;
            List<Tree> rightTrees = new ArrayList<Tree>();

            rightTrees.addAll(method.getParameters());
            rightTrees.addAll(method.getThrows());
            rightTrees.add(method.getBody());

            Name name = method.getName();
            
            if (method.getReturnType() == null)
                name = ((ClassTree) decl.getParentPath().getLeaf()).getSimpleName();
            
            return findIdentifierSpanImpl(info, leaf, method.getReturnType(), rightTrees, name.toString(), info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(VariableTree.class).contains(leaf.getKind())) {
            VariableTree var = (VariableTree) leaf;

            return findIdentifierSpanImpl(info, leaf, var.getType(), Collections.singletonList(var.getInitializer()), var.getName().toString(), info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(MemberSelectTree.class).contains(leaf.getKind())) {
            return findIdentifierSpanImpl(info, (MemberSelectTree) leaf, info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(ClassTree.class).contains(leaf.getKind())) {
            String name = ((ClassTree) leaf).getSimpleName().toString();
            
            if (name.length() == 0)
                return null;
            
            SourcePositions positions = info.getTrees().getSourcePositions();
            CompilationUnitTree cu = info.getCompilationUnit();
            int start = (int)positions.getStartPosition(cu, leaf);
            int end   = (int)positions.getEndPosition(cu, leaf);
            
            if (start == (-1) || end == (-1)) {
                return null;
            }
            
            return findTokenWithText(info, name, start, end);
        }
        throw new IllegalArgumentException("Only MethodDecl, VariableDecl and ClassDecl are accepted by this method.");
    }

    public static int[] findIdentifierSpan( final TreePath decl, final CompilationInfo info, final Document doc) {
        final int[] result = new int[] {-1, -1};
        doc.render(new Runnable() {
            public void run() {
                Token<JavaTokenId> t = findIdentifierSpan(info, doc, decl);
                if (t != null) {
                    result[0] = t.offset(null);
                    result[1] = t.offset(null) + t.length();
                }
            }
        });
        
        return result;
    }
    
    public static Token<JavaTokenId> findIdentifierSpan(final CompilationInfo info, final Document doc, final TreePath decl) {
        @SuppressWarnings("unchecked")
        final Token<JavaTokenId>[] result = new Token[1];
        doc.render(new Runnable() {
            public void run() {
                result[0] = findIdentifierSpanImpl(info, decl);
            }
        });
        
        return result[0];
    }
    
    private static int findBodyStartImpl(Tree cltree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, cltree);
        int end   = (int)positions.getEndPosition(cu, cltree);
        
        if (start == (-1) || end == (-1)) {
            return -1;
        }
        
        if (start > doc.getLength() || end > doc.getLength()) {
            if (DEBUG) {
                System.err.println("Log: position outside document: ");
                System.err.println("decl = " + cltree);
                System.err.println("startOffset = " + start);
                System.err.println("endOffset = " + end);
                Thread.dumpStack();
            }
            
            return (-1);
        }
        
        try {
            String text = doc.getText(start, end - start);
            
            int index = text.indexOf('{');
            
            if (index == (-1)) {
                return -1;
//                throw new IllegalStateException("Should NEVER happen.");
            }
            
            return start + index;
        } catch (BadLocationException e) {
            LOG.log(Level.INFO, null, e);
        }
        
        return (-1);
    }
    
    public static int findBodyStart(final Tree cltree, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        Kind kind = cltree.getKind();
        if (kind != Kind.CLASS && kind != Kind.METHOD)
            throw new IllegalArgumentException("Unsupported kind: "+ kind);
        final int[] result = new int[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = findBodyStartImpl(cltree, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static int findLastBracketImpl(MethodTree tree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, tree);
        int end   = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || end == (-1)) {
            return -1;
        }
        
        if (start > doc.getLength() || end > doc.getLength()) {
            if (DEBUG) {
                System.err.println("Log: position outside document: ");
                System.err.println("decl = " + tree);
                System.err.println("startOffset = " + start);
                System.err.println("endOffset = " + end);
                Thread.dumpStack();
            }
            
            return (-1);
        }
        
        try {
            String text = doc.getText(end - 1, 1);
            
            if (text.charAt(0) == '}')
                return end - 1;
        } catch (BadLocationException e) {
            LOG.log(Level.INFO, null, e);
        }
        
        return (-1);
    }
    
    public static int findLastBracket(final MethodTree tree, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        final int[] result = new int[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = findLastBracketImpl(tree, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static Token<JavaTokenId> createHighlightImpl(CompilationInfo info, Document doc, TreePath tree) {
        Tree leaf = tree.getLeaf();
        SourcePositions positions = info.getTrees().getSourcePositions();
        CompilationUnitTree cu = info.getCompilationUnit();
        
        //XXX: do not use instanceof:
        if (leaf instanceof MethodTree || leaf instanceof VariableTree || leaf instanceof ClassTree || leaf instanceof MemberSelectTree) {
            return findIdentifierSpan(info, doc, tree);
        }
        
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf);
        
        TokenHierarchy<Void> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
        
        if (ts.move(start) == Integer.MAX_VALUE) {
            return null;
        }
        
        ts.moveNext();
        
        if (ts.offset() == start && ts.token().id() == JavaTokenId.IDENTIFIER) {
             return ts.token();
        }
        
        return null;
    }
    
    public static Token<JavaTokenId> getToken(final CompilationInfo info, final Document doc, final TreePath tree) {
        @SuppressWarnings("unchecked")
        final Token<JavaTokenId>[] result = new Token[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = createHighlightImpl(info, doc, tree);
            }
        });
        
        return result[0];
    }
    
    private static final Set<String> keywords;
    
    static {
        keywords = new HashSet<String>();
        
        keywords.add("true");
        keywords.add("false");
        keywords.add("null");
        keywords.add("this");
        keywords.add("super");
        keywords.add("class");
    }
    
    public static boolean isKeyword(Tree tree) {
        if (tree.getKind() == Kind.IDENTIFIER) {
            return keywords.contains(((IdentifierTree) tree).getName().toString());
        }
        if (tree.getKind() == Kind.MEMBER_SELECT) {
            return keywords.contains(((MemberSelectTree) tree).getIdentifier().toString());
        }
        
        return false;
    }
    
    public static boolean isPrivateElement(Element el) {
        if (el.getKind() == ElementKind.PARAMETER)
            return true;
        
        if (el.getKind() == ElementKind.LOCAL_VARIABLE)
            return true;
        
        if (el.getKind() == ElementKind.EXCEPTION_PARAMETER)
            return true;
        
        return el.getModifiers().contains(Modifier.PRIVATE);
    }
    

}
