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

package org.netbeans.modules.javascript.editing;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.util.Exceptions;

/**
 * AST based formatter, walks over AST tree together with lexer tokens
 * 
 * @author Martin Adamek
 */
public class JsPretty {

    private final CompilationInfo info;
    private final BaseDocument doc;
    private final TokenSequence<? extends JsTokenId> ts;
    private final int begin;
    private final int end;
    private final LinkedList<Diff> diffs = new LinkedList<Diff>();
    private int indent;
    // marker for last handled offset, we don't want to handle token twice
    private int lastHandledOffset = -1;
    private final int indentSize;
    private final int continuationIndentSize;
    private final int tabSize;
    
    public JsPretty(CompilationInfo info, BaseDocument document, int begin, int end, int indentSize, int continuationIndentSize) {
        this.info = info;
        this.doc = document;
        this.begin = begin;
        this.end = end;
        this.ts = LexUtilities.getPositionedSequence(doc, 0, false);
        this.indentSize = indentSize;
        this.continuationIndentSize = continuationIndentSize;
        //Preferences prefs = MimeLookup.getLookup(MimePath.get(JsTokenId.JAVASCRIPT_MIME_TYPE)).lookup(Preferences.class);
        // Tab settings are still global...
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        this.tabSize = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, 4);
    }

    public void format() {
        JsParseResult result = AstUtilities.getParseResult(info);
        Node root = result.getRootNode();
        if (root != null) {
                walk(root);
        }
        acceptNode((Node)null);
    }

    private void walk(Node root) {

        // skip some invisible nodes that jump to some high offsets
        if (ignoredType(root.getType())) {
            return;
        }

        acceptNode(root);
        
        if (root.hasChildren()) {
            Node child = root.getFirstChild();
            switch (root.getType()) {
                case Token.BLOCK:
                    if (child.getType() == Token.IFNE) {
                        visitIf(root);
                    } else {
                        visitOtherBlock(root);
                    }
                    break;
                case Token.LOOP:
                    JsTokenId id = ts.token().id();
                    if (id == JsTokenId.WHILE) {
                        visitWhile(root);
                    } else if (id == JsTokenId.FOR) {
                        visitOtherBlock(root);
                    } else if (child.getType() == Token.VAR) {
                        visitOtherBlock(root);
                    } else {
                        visitDefault(root);
                    }
                    break;
                case Token.EXPR_RESULT:
                    visitContinuation(root);
                    break;
                case Token.CASE:
                case Token.DEFAULT:
                    visitCase(root);
                    break;
                case Token.SWITCH:
                    visitOtherBlock(root);
                    break;
                case Token.OBJECTLIT:
                    visitObjectLit(root);
                    break;
                case Token.WITH:
                    visitWith(root);
                    break;
                case Token.TRY:
                    visitBlockWithoutIndenting(root);
                    break;
                case Token.CATCH:
                    visitCatch(root);
                    break;
                default:
                    visitDefault(root);
            }
        }
    }

    private Node next(Node child) {
        if (child == null) {
            return null;
        }
        do {
            child = child.getNext();
        } while (child != null && ignoredType(child.getType()));
        
        return child;
    }
    
    private boolean ignoredType(int type) {
        switch (type) {
            case Token.JSR:
            case Token.GOTO:
            case Token.TARGET:
            case Token.OBJLITNAME:
                return true;
            default:
                return false;
        }
    }
    
    private void visitDefault(Node node) {
        if (node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                walk(child);
                child = next(child);
            }
        }
        acceptOffset(node.getSourceEnd());
    }
    
    private void visitContinuation(Node node) {
        increaseContinuation("visitContinuation " + node);
        if (node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                walk(child);
                child = next(child);
            }
        }
        decreaseContinuation("visitContinuation");
        acceptOffset(node.getSourceEnd());
    }
    
    private void visitObjectLit(Node root) {
        increaseIndent("visitObjectLit");
        if (root.hasChildren()) {
            boolean firstObjectLitName = true;
            Node child = root.getFirstChild();
            while (child != null) {
                if (child.getType() == Token.OBJLITNAME) {
                    if (firstObjectLitName) {
                        firstObjectLitName = false;
                    } else {
                        TokenSequence<? extends JsTokenId> positionedTs = LexUtilities.getPositionedSequence(doc, child.getSourceStart());
                        positionedTs.movePrevious();
                        org.netbeans.api.lexer.Token<? extends JsTokenId> previous = LexUtilities.findPrevious(positionedTs, 
                                Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.LINE_COMMENT, JsTokenId.BLOCK_COMMENT));
                        if (previous.id() != JsTokenId.EOL) {
                            addDiff(child.getSourceStart(), child.getSourceStart(), "\n" + getIndent(), "visitObjectLit");
                        }
                    }
                }
                walk(child);
                // don't use next(Node), because OBJLITNAME is ignored there
                child = child.getNext();
            }
        }
        decreaseIndent("visitObjectLit");
        acceptOffset(root.getSourceEnd());
    }
    
    private void visitOtherBlock(Node root) {
        if (root.hasChildren()) {
            increaseIndent("visitOtherBlock");
            Node child = root.getFirstChild();
            while (child != null) {
                if (child.getSourceEnd() - child.getSourceStart() > 0) {
                    walk(child);
                }
                child = next(child);
            }
            decreaseIndent("visitOtherBlock");
        }
        acceptOffset(root.getSourceEnd());
    }
    
    private void visitBlockWithoutIndenting(Node node) {
        if (node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                walk(child);
                child = next(child);
            }
        }
        acceptOffset(node.getSourceEnd());
    }
    
    private void visitCatch(Node node) {
        increaseIndent("visitCatch");
        if (node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                if (child.getType() == Token.BLOCK) {
                    visitBlockWithoutIndenting(child);
                } else {
                    walk(child);
                }
                child = next(child);
            }
        }
        decreaseIndent("visitCatch");
        acceptOffset(node.getSourceEnd());
    }
    
    private void visitWith(Node node) {
        if (node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                if (child.getType() == Token.BLOCK) {
                    visitBlockWithoutIndenting(child);
                } else {
                    walk(child);
                }
                child = next(child);
            }
        }
        acceptOffset(node.getSourceEnd());
    }
    
    private void visitCase(Node node) {
        increaseIndent("visitCase");
        if (node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                if (child.getType() == Token.BLOCK) {
                    visitBlockWithoutIndenting(child);
                } else {
                    walk(child);
                }
                child = next(child);
            }
        }
        decreaseIndent("visitCase");
        acceptOffset(node.getSourceEnd());
    }
    
    private void visitWhile(Node node) {
        Node child = node.getFirstChild();
        if (ignoredType(child.getType())) {
            walk(child);
            child = next(child);
        }
        if (isWithoutBlock(child)) {
            increaseIndent("visitWhile");
            walk(child);
            decreaseIndent("visitWhile");
            child = next(child);
        } else {
        }
        while (child != null) {
            walk(child);
            child = next(child);
        }
    }
    
    private void visitIf(Node node) {
        
        Node child = node.getFirstChild();
        assert child.getType() == Token.IFNE;
        walk(child);

        child = next(child);
        if (isWithoutBlock(child)) {
            increaseIndent("visitIf 1");
            walk(child);
            decreaseIndent("visitIf 1 after " + child);
            child = next(child);
        } else {
            walk(child);
            child = next(child);
        }

        // check for else
        if (child != null && child.getType() != Token.BLOCK) {
            // I cannot walk() right now, because ELSE would be ignored
            // let's just move to ELSE, so EOLs before it will be managed correctly
            // and we will have correct indent
            acceptToken(JsTokenId.ELSE);
            increaseIndent("visitIf 2");
            walk(child);
            decreaseIndent("visitIf 2");
            child = next(child);
        }
        
        while (child != null) {
            walk(child);
            child = next(child);
        }
    }
    
    private void acceptNode(Node node) {
        
        // used when we want to finish whole token sequence, even after last AST node was visited
        if (node == null) {
            node = new Node(Token.ERROR);
        } else {
            if (node.getType() == Token.REGEXP && node.getSourceEnd() > ts.offset()) {
                // skip regexp
                acceptOffset(node.getSourceStart());
                ts.move(node.getSourceEnd());
                ts.moveNext();
                return;
            }
            if (node.getSourceStart() < ts.offset()) {
                return;
            }
            Node child = node.getFirstChild();
            if (child != null) {
                if (node.getSourceStart() > child.getSourceStart()) {
                    return;
                } else if (node.getType() == Token.CALL) {
                    return;
                }
            }
        }
        int nodeOffset = node.getSourceStart();
        do {
            org.netbeans.api.lexer.Token<? extends JsTokenId> token = ts.token();
            int tokenOffset = ts.offset();
//            System.out.println("\t# acceptNode " + node + ", " + token.id() + ", " + ts.offset());
            if (tokenOffset >= nodeOffset) {
                if (token.id() != JsTokenId.EOL && token.id() != JsTokenId.LBRACE) {
                    handleToken(token, "1 acceptToken(" + node + ")");
                }
                return;
            }
            handleToken(token, "2 acceptToken(" + node + ")");
        } while (ts.moveNext());
    }

    /**
     * used for unvisiting
     */
    private void acceptOffset(int targetOffset) {
        do {
            org.netbeans.api.lexer.Token<? extends JsTokenId> token = ts.token();
            int tokenOffset = ts.offset();
//            System.out.println("\t# acceptOffset " + targetOffset + ", " + ts.offset());
            if (tokenOffset >= targetOffset) {
                if (token.id() != JsTokenId.EOL && token.id() != JsTokenId.LBRACE) {
                    handleToken(token, "1 acceptToken(" + targetOffset + ")");
                }
                return;
            }
            handleToken(token, "2 acceptOffset(" + targetOffset + ")");
        } while (ts.moveNext());
    }

     private void acceptToken(JsTokenId jsTokenId) {
        do {
            org.netbeans.api.lexer.Token<? extends JsTokenId> token = ts.token();
            JsTokenId id = token.id();
//            System.out.println("\t# acceptToken " + jsTokenId + ", " + ts.offset());
            if (id == jsTokenId) {
                if (token.id() != JsTokenId.EOL && token.id() != JsTokenId.LBRACE) {
                    handleToken(token, "1 acceptToken(" + jsTokenId + ")");
                }
                ts.moveNext();
                return;
            }
            handleToken(token, "2 acceptToken(" + jsTokenId + ")");
        } while (ts.moveNext());
    }

    private void handleToken(org.netbeans.api.lexer.Token<? extends JsTokenId> token, String caller) {
        int tokenOffset = ts.offset();
        
        if (tokenOffset <= lastHandledOffset) {
            return;
        }
        lastHandledOffset = tokenOffset;
        JsTokenId id = token.id();
        int tokenLength = token.length();
        
        switch (id) {
            case WHITESPACE:
                // if there is a space at the beginning (before any node)
                if (tokenOffset == 0) {
                    addDiff(tokenOffset, tokenOffset + tokenLength, "", "handleToken WHITESPACE");
                }
                break;
            case EOL:
                // note: sometimes EOL includes WSs, e.g. "  \n"
                int offset = tokenOffset + tokenLength;
                // if LBRACE is on new line, I don't want to increase indent
                // pretend there's no EOL to increase() function
                boolean lbraceOnNewLine = false;
                // don't add indent if newline is last character in formatted section
                if (offset < end) {
                    try {
                        int firstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                        if (firstNonWhite != -1) {
                            int nextLineIndent = firstNonWhite - offset;
                            TokenSequence<? extends JsTokenId> positionedTs = LexUtilities.getPositionedSequence(doc, tokenOffset, false);
                            positionedTs.moveNext();
                            org.netbeans.api.lexer.Token<? extends JsTokenId> next = LexUtilities.findNextNonWsNonComment(positionedTs);
                            String indentString = null;
                            if (indent > 0 && next.id() == JsTokenId.LBRACE && !diffs.isEmpty()) {
                                lbraceOnNewLine = true;
                                // dirty trick - take indent from previous line if LBRACE is on new line
                                indentString = diffs.getFirst().text;
                            } else {
                                indentString = getIndent();
                            }
                            
                            addDiff(offset, offset + nextLineIndent, indentString, "handleToken EOL - " + ts.offset());
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
                if (!lbraceOnNewLine) {
                    newLine("handleToken EOL");
                }
                break;
            case LPAREN:
                increaseContinuation("handleToken LPAREN");
                break;
            case RPAREN:
                decreaseContinuation("handleToken RPAREN");
                break;
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                break;
            case LBRACE:
                TokenSequence<? extends JsTokenId> positionedTs = LexUtilities.getPositionedSequence(doc, tokenOffset);
                positionedTs.moveNext();
                org.netbeans.api.lexer.Token<? extends JsTokenId> next = LexUtilities.findNext(positionedTs, 
                        Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.LINE_COMMENT, JsTokenId.BLOCK_COMMENT));
                offset = positionedTs.offset();
                if (next.id() != JsTokenId.EOL && next.id() != JsTokenId.RBRACE) {
                    addDiff(offset, offset, "\n" + getIndent(), "handleToken LBRACE");
                    newLine("handleToken LBRACE");
                }
                break;
            case RBRACE:
                // add newline before
                positionedTs = LexUtilities.getPositionedSequence(doc, tokenOffset);
                offset = positionedTs.offset();
                positionedTs.movePrevious();
                org.netbeans.api.lexer.Token<? extends JsTokenId> previous = LexUtilities.findPrevious(positionedTs, 
                        Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.LINE_COMMENT, JsTokenId.BLOCK_COMMENT));
                if (previous.id() != JsTokenId.EOL && previous.id() != JsTokenId.LBRACE) {
                    boolean decreased = false;
                        addDiff(offset, offset, "\n" + getIndent(), "handleToken RBRACE");
                        newLine("handleToken RBRACE");
                    if (decreased) {
                        increaseIndent("handleToken RBRACE");
                    }
                }
                break;
        }
    }
     
    private boolean isWithoutBlock(Node node) {
        boolean result = false;
        if (node.getType() == Token.BLOCK) {
            int offset = ts.offset();
            ts.move(node.getSourceStart());
            ts.moveNext();
            if (ts.token().id() != JsTokenId.LBRACE) {
                result = true;
            }
            ts.move(offset);
            ts.moveNext();
        } else {
            result = true;
        }
        return result;
    }

    private class StackItem {
        int indent;
        boolean tip = true;
        StackItem(int indent) {
            this.indent = indent;
        }
        @Override
        public String toString() {
            return "[" + indent + "," + tip + "]";
        }
    }
    
    private Stack<StackItem> stack = new Stack<StackItem>();
    private boolean isIncreasingLine = false;
    
    private void newLine(String caller) {
//        System.out.println("> newLine() - " + caller);
        isIncreasingLine = false;
    }

    private void increase(int size, String caller) {
        if (!stack.isEmpty() && isIncreasingLine) {
            stack.peek().tip = false;
            indent -= stack.peek().indent;
        }
        indent += size;
        stack.push(new StackItem(size));
        isIncreasingLine = true;
//        System.out.println("# increase(" + size + ") to " + indent + " - " + caller + ", " + ts.offset() + ", " + stack);
    }
    
    private void decrease(String caller) {
        StackItem stackItem = stack.pop();
        if (isIncreasingLine) {
            indent -= stackItem.indent;
            assert indent >= 0;
            if (!stack.isEmpty() && !stack.peek().tip) {
                stack.peek().tip = true;
                indent += stack.peek().indent;
            } else {
                isIncreasingLine = false;
            }
        } else {
            if (stackItem.tip) {
                indent -= stackItem.indent;
                assert indent >= 0;
            }
        }
//        System.out.println("# decrease() to " + indent + " - " + caller + ", " + ts.offset());
    }
    
    private void increaseIndent(String caller) {
        increase(indentSize, caller);
    }

    private void increaseContinuation(String caller) {
        increase(continuationIndentSize, caller);
    }
    
    private void decreaseIndent(String caller) {
        decrease(caller);
    }

    private void decreaseContinuation(String caller) {
        decrease(caller);
    }
    
    private String getIndent() {

        // TODO: use global settings
        boolean expandTabToSpaces = true;

        StringBuilder sb = new StringBuilder();
        int col = 0;
        if (!expandTabToSpaces) {
            while (col + tabSize <= indent) {
                sb.append('\t'); //NOI18N

                col += tabSize;
            }
        }
        while (col < indent) {
            sb.append(" "); //NOI18N

            col++;
        }
        return sb.toString();
    }

    private void addDiff(int start, int end, String text, String caller) {

        if (start < this.begin || end > this.end) {
            return;
        }
        
//        System.out.println("> addDiff(" + start + "," + end + ",[" + text + "]) - " + caller);

        if (end < doc.getLength()) {
            diffs.addFirst(new Diff(start, end, text));
        }
    }

    LinkedList<Diff> getDiffs() {
        return diffs;
    }

    static class Diff {

        final int start;
        final int end;
        final String text;

        Diff(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        @Override
        public String toString() {
            return "Diff<" + start + "," + end + ">: [" + text + "]"; //NOI18N
        }
    }

}
