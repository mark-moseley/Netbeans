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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Node.LabelledNode;
import org.mozilla.javascript.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.fpi.gsf.OffsetRange;
import org.netbeans.fpi.gsf.Parser;
import org.netbeans.fpi.gsf.ParserFile;
import org.netbeans.fpi.gsf.ParserResult;
import org.netbeans.fpi.gsf.SourceFileReader;
import org.netbeans.fpi.gsf.TranslatedSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.sfpi.gsf.DefaultParseListener;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class AstUtilities {
    public static int getAstOffset(CompilationInfo info, int lexOffset) {
        ParserResult result = info.getEmbeddedResult(JsMimeResolver.JAVASCRIPT_MIME_TYPE, 0);
        if (result != null) {
            TranslatedSource ts = result.getTranslatedSource();
            if (ts != null) {
                return ts.getAstOffset(lexOffset);
            }
        }
              
        return lexOffset;
    }

    public static OffsetRange getAstOffsets(CompilationInfo info, OffsetRange lexicalRange) {
        ParserResult result = info.getEmbeddedResult(JsMimeResolver.JAVASCRIPT_MIME_TYPE, 0);
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
                    return new OffsetRange(start, start+lexicalRange.getLength());
                }
            }
        }
        return lexicalRange;
    }

    /** 
     * Return the comment sequence (if any) for the comment prior to the given offset.
     */
    public static TokenSequence<? extends JsCommentTokenId> getCommentFor(CompilationInfo info, BaseDocument doc, FunctionNode node) {
        int astOffset = node.getSourceStart();
        int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
        if (lexOffset == -1) {
            return null;
        }
        
        try {
            // Jump to the end of the previous line since that's typically where the block comments
            // sit (I can't just iterate left in the document hierarchy since for functions in 
            // object literals there could be names there -- e.g.
            //     /** My document */
            //     foo: function() {
            //     }
            // Here the function offset points to the beginning of "function", not "foo".
            int rowStart = Utilities.getRowStart(doc, lexOffset);
            if (rowStart > 0) {
                lexOffset = Utilities.getRowEnd(doc, rowStart-1);
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        return LexUtilities.getCommentFor(doc, lexOffset);
    }
    
    public static Node getFirstChild(Node node) {
        return node.getFirstChild();
    }

    public static Node getSecondChild(Node node) {
        Node first = node.getFirstChild();
        if (first == null) {
            return null;
        } else {
            return first.getNext();
        }
    }
    
    public static Node getRoot(CompilationInfo info) {
//        ParserResult result = info.getParserResult();
//
//        if (result == null) {
//            return null;
//        }
//
//        return getRoot(result);
        return getRoot(info, JsMimeResolver.JAVASCRIPT_MIME_TYPE);
    }

    public static JsParseResult getParseResult(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(JsMimeResolver.JAVASCRIPT_MIME_TYPE, 0);

        if (result == null) {
            return null;
        } else {
            return ((JsParseResult)result);
        }
    }

    public static Node getRoot(CompilationInfo info, String mimeType) {
        ParserResult result = info.getEmbeddedResult(mimeType, 0);

        if (result == null) {
            return null;
        }
        
        return getRoot(result);
    }
    
    public static Node getRoot(ParserResult r) {
        assert r instanceof JsParseResult;

        JsParseResult result = (JsParseResult)r;
        
        return result.getRootNode();
    }

    public static Node getForeignNode(final IndexedFunction o, Node[] foreignRootRet) {
        ParserFile file = o.getFile();

        if (file == null) {
            return null;
        }

        List<ParserFile> files = Collections.singletonList(file);
        SourceFileReader reader =
            new SourceFileReader() {
                public CharSequence read(ParserFile file)
                    throws IOException {
                    Document doc = o.getDocument();

                    if (doc == null) {
                        return "";
                    }

                    try {
                        return doc.getText(0, doc.getLength());
                    } catch (BadLocationException ble) {
                        IOException ioe = new IOException();
                        ioe.initCause(ble);
                        throw ioe;
                    }
                }

                public int getCaretOffset(ParserFile fileObject) {
                    return -1;
                }
            };

        DefaultParseListener listener = new DefaultParseListener();

        // TODO - embedding model?
TranslatedSource translatedSource = null; // TODO - determine this here?                
        Parser.Job job = new Parser.Job(files, listener, reader, translatedSource);
        new JsParser().parseFiles(job);

        ParserResult result = listener.getParserResult();

        if (result == null) {
            return null;
        }

        Node root = AstUtilities.getRoot(result);

        if (root == null) {
            return null;
        } else if (foreignRootRet != null) {
            foreignRootRet[0] = root;
        }

        String signature = o.getSignature();

        if (signature == null) {
            return null;
        }

//        Node node = AstUtilities.findBySignature(root, signature);
        JsParseResult rpr = (JsParseResult)result;
        for (AstElement element : rpr.getStructure().getElements()) {
            if (element instanceof FunctionAstElement) {
                FunctionAstElement func = (FunctionAstElement) element;
                if (signature.equals(func.getSignature())) {
                    return func.getNode();
                }
            }
        }
        
        return null;
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getRange(CompilationInfo info, Node node) {
        if (node.getType() == Token.STRING) {
            // Work around difficulties with the offset
            try {
                BaseDocument doc = (BaseDocument) info.getDocument();
                int astOffset = node.getSourceStart();
                int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
                if (lexOffset != -1) {
                    int rowStart = Utilities.getRowStart(doc, lexOffset);
                    int rowEnd = Utilities.getRowEnd(doc, rowStart);
                    String line = doc.getText(rowStart, rowEnd-rowStart);
                    String s = node.getString();
                    int lineOffset = line.indexOf(s);
                    if (lineOffset != -1) {
                        int start = rowStart+lineOffset;
                        int astStart = getAstOffset(info, start);
                        if (astStart != -1) {
                            return new OffsetRange(astStart, astStart+s.length());
                        }
                    }
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return new OffsetRange(node.getSourceStart(), node.getSourceEnd());
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getNameRange(Node node) {
        final int type = node.getType();
        switch (type) {
        case Token.NAME:
        case Token.BINDNAME:
        case Token.FUNCNAME:
        case Token.PARAMETER:
        case Token.OBJLITNAME:
            int start = node.getSourceStart();
            String name = node.getString();
            return new OffsetRange(start, start+name.length());
        case Token.CALL:
            Node nameNode = findCallNameNode(node);
            if (nameNode != null) {
                return getNameRange(nameNode);
            }
        }

        return getRange(node);
    }

    /**
     * Look for the caret offset in the parameter list; return the
     * index of the parameter that contains it.
     */
    public static int findArgumentIndex(Node call, int astOffset, AstPath path) {
        assert call.getType() == Token.CALL;

        // The first child is the call expression -- the name, or property lookup etc.
        Node child = call.getFirstChild();
        if (child == null) {
            return 0;
        }
        child = child.getNext();

        int index = 0;
        while (child != null) {
            if (child.getSourceEnd() >= astOffset) {
                return index;
            }
            child = child.getNext();
            if (child != null) {
                index++;
            }
        }
        
        return index;
    }
    
    private static Node findCallNameNode(Node callNode) {
        if (callNode.hasChildren()) {
            Node child = callNode.getFirstChild();

            if (child != null) {
                if (child.getType() == Token.NAME) {
                    return child;
                } else if (child.getType() == Token.GETPROP) {
                    Node grandChild = child.getFirstChild();
                    assert grandChild.getNext().getNext() == null : grandChild.getNext().getNext();
                    return grandChild.getNext();
                }
            } else {
                assert false : "Unexpected call firstchild - " + child;
            }
        }

        return null;
    }
    
    /** Return the name of a method being called.
     * @param callNode the node with type == Token.CALL
     * @param fqn If true, return the full property name to the function being called,
     *   otherwise return just the basename.
     *   (Note - this isn't necessarily the function name...
     *      function foo(x,y) {
     *      }
     *      bar = foo
     *      bar()   --- here we're really calling the method named foo but getCallName will
     *                   return bar
     */
    public static String getCallName(Node callNode, boolean fqn) {
        assert callNode.getType() == Token.CALL;
        
        if (!fqn) {
            Node nameNode = findCallNameNode(callNode);
            if (nameNode != null) {
                return nameNode.getString();
            }
        } else if (callNode.hasChildren()) {
            Node child = callNode.getFirstChild();
            
            if (child != null) {
                if (child.getType() == Token.GETELEM) {
                    child = child.getNext();
                }
                if (child.getType() == Token.NAME) {
                    return child.getString();
                } else if (child.getType() == Token.GETPROP) {
                    Node grandChild = child.getFirstChild();
                    if (fqn) {
                        StringBuilder sb = new StringBuilder();
                        
                        if (grandChild instanceof Node.StringNode) {
                            sb.append(grandChild.getString());
                            sb.append(".");
                        }
                        sb.append(grandChild.getNext().getString());
                        assert grandChild.getNext().getNext() == null : grandChild.getNext().getNext();
                        return sb.toString();
                    } else {
                        assert grandChild.getNext().getNext() == null : grandChild.getNext().getNext();
                        return grandChild.getNext().getString();
                    }
                } else {
                    assert false : "Unexpected call firstchild - " + child;
                }
            }
        }
        
        return ""; // NOI18N
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getRange(Node node) {
        assert node.getSourceEnd() >= node.getSourceStart() : "Invalid offsets for " + node;
        return new OffsetRange(node.getSourceStart(), node.getSourceEnd());
    }
    
    public static FunctionNode findMethodAtOffset(Node root, int offset) {
        AstPath path = new AstPath(root, offset);
        Iterator<Node> it = path.leafToRoot();

        while (it.hasNext()) {
            Node node = it.next();

            if (node.getType() == Token.FUNCTION) {
                return (FunctionNode)node;
            }
        }

        return null;
    }

    
    
    public static Node findLocalScope(Node node, AstPath path) {
        // TODO - implement properly (using the VariableFinder)

        Iterator<Node> it = path.leafToRoot();
        while (it.hasNext()) {
            Node n = it.next();
            if (n.getType() == Token.FUNCTION) {
                return n;
            }
            
            // This isn't really right - there could be a series of nested functions!
        }
        
        
//        Node method = findMethod(path);
//
//        if (method == null) {
//            Iterator<Node> it = path.leafToRoot();
//            while (it.hasNext()) {
//                Node n = it.next();
//                switch (n.nodeId) {
//                case NodeTypes.DEFNNODE:
//                case NodeTypes.DEFSNODE:
//                case NodeTypes.CLASSNODE:
//                case NodeTypes.SCLASSNODE:
//                case NodeTypes.MODULENODE:
//                    return n;
//                }
//            }
//            
//            if (path.root() != null) {
//                return path.root();
//            }
//
//            method = findBlock(path);
//        }
//
//        if (method == null) {
//            method = path.leafParent();
//
//            if (method.nodeId == NodeTypes.NEWLINENODE) {
//                method = path.leafGrandParent();
//            }
//
//            if (method == null) {
//                method = node;
//            }
//        }
//
//        return method;
        
// TODO JS - implement properly        
        return path.root();
        
    }
    
    public static boolean isNameNode(Node node) {
        int type = node.getType();
        return type == Token.NAME || type == Token.BINDNAME || type == Token.PARAMETER || 
                    type == Token.FUNCNAME || type == Token.OBJLITNAME /*|| type == Token.CALL*/;
    }
    
    /** Collect nodes of the given types (node.nodeId==NodeTypes.x) under the given root */
    public static void addNodesByType(Node root, int[] nodeIds, List<Node> result) {
        for (int i = 0; i < nodeIds.length; i++) {
            if (root.getType() == nodeIds[i]) {
                result.add(root);
                break;
            }
        }

        if (root.hasChildren()) {
            Node child = root.getFirstChild();

            for (; child != null; child = child.getNext()) {
                addNodesByType(child, nodeIds, result);
            }
        }
    }
    
    public static boolean isLabelledFunction(Node objlitNode) {
        assert objlitNode.getType() == Token.OBJLITNAME;
        LabelledNode node = (LabelledNode)objlitNode;
        Node labelledNode = node.getLabelledNode();
        return labelledNode.getType() == Token.FUNCTION;
    }

    public static FunctionNode getLabelledFunction(Node objlitNode) {
        assert objlitNode.getType() == Token.OBJLITNAME;
        LabelledNode node = (LabelledNode)objlitNode;
        Node labelledNode = node.getLabelledNode();
        if (labelledNode.getType() == Token.FUNCTION) {
            return (FunctionNode)labelledNode;
        }
        
        return null;
    }
}
