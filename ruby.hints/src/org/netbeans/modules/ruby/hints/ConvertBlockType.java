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
package org.netbeans.modules.ruby.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.ast.IArgumentNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.YieldNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.EditList;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.PreviewableFix;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Offer to convert a {}-style block into do-end, or vice versa
 *
 * @author Tor Norbye
 */
public class ConvertBlockType implements AstRule {

    public ConvertBlockType() {
    }

    public boolean appliesTo(CompilationInfo info) {
        // Skip for RHTML files for now - isn't implemented properly
        return info.getFileObject().getMIMEType().equals("text/x-ruby");
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.ITERNODE);
    }

    public void run(RuleContext context, List<Description> result) {
        Node node = context.node;
        CompilationInfo info = context.compilationInfo;
        int caretOffset = context.caretOffset;
        
        assert (node.nodeId == NodeTypes.ITERNODE);
        try {
            int astOffset = node.getPosition().getStartOffset();
            int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (lexOffset == -1 || lexOffset > doc.getLength() - 1) {
                return;
            }

            // Limit the hint to the -opening- line of the block
            boolean caretOnStart = true;
            final int beginRowEnd = Utilities.getRowEnd(doc, lexOffset);
            final int caretRowEnd = Utilities.getRowEnd(doc, caretOffset);
            boolean caretLine = beginRowEnd == caretRowEnd;
            int endLexOffset = -1;
            if (!caretLine) {
                // ...or the -ending- line of the block
                int endAstOffset = node.getPosition().getEndOffset();
                endLexOffset = LexUtilities.getLexerOffset(info, endAstOffset);
                if (endLexOffset == -1) {
                    return;
                }
                int endRowEnd = endLexOffset;
                if (endRowEnd < doc.getLength()) {
                    endRowEnd = Utilities.getRowEnd(doc, endLexOffset);
                }
                caretLine = endRowEnd == caretRowEnd;
                if (!caretLine) {
                    return;
                }
                if (endRowEnd != beginRowEnd) {
                    caretOnStart = false;
                }
            }

            Token<? extends RubyTokenId> token = LexUtilities.getToken(doc, lexOffset);
            if (token == null) {
                return;
            }
            
            TokenId id = token.id();
            if (id == RubyTokenId.LBRACE || id == RubyTokenId.DO) {
                OffsetRange range;
                if (caretOnStart) {
                    range = new OffsetRange(lexOffset, lexOffset + token.length());
                } else {
                    assert endLexOffset != -1;
                    int len = (id == RubyTokenId.LBRACE) ? 1 : 3; // }=1, end=3
                    range = new OffsetRange(endLexOffset-len, endLexOffset);
                }
                List<Fix> fixList = new ArrayList<Fix>(1);
                boolean convertFromBrace = id == RubyTokenId.LBRACE;

                int endOffset = node.getPosition().getEndOffset();
                if (endOffset > doc.getLength()) {
                    endOffset = doc.getLength();
                }

                // See if we should offer to collapse
                String text = doc.getText(lexOffset, endOffset - lexOffset);
                int nonspaceChars = 0;
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (!Character.isWhitespace(c)) {
                        nonspaceChars++;
                    }
                }
                int startColumn = lexOffset - Utilities.getRowStart(doc, lexOffset);
                // Not yet exposed from the Ruby module
                //int rightMargin = org.netbeans.modules.ruby.options.CodeStyle.getDefault(null).getRightMargin();
                // #119151: This should be available for a lot of hints that don't neatly fit.
                // So only suppress it for -really- large blocks.
                int rightMargin = 350;
                boolean offerCollapse = rightMargin > startColumn + nonspaceChars;

                // TODO - in an RHTML page, make sure there are no "gaps" (non Ruby code) between the do and the end,
                // since we can't handle those for collapse
                // TODO
                
                boolean sameLine = Utilities.getRowEnd(doc, lexOffset) == Utilities.getRowEnd(doc, endOffset);
                if (sameLine && convertFromBrace) {
                    fixList.add(new ConvertTypeFix(info, node, convertFromBrace, !convertFromBrace, true, false));
                } else if (!sameLine && !convertFromBrace && offerCollapse) {
                    fixList.add(new ConvertTypeFix(info, node, convertFromBrace, !convertFromBrace, false, true));
                } // else: Should I let you expand a single line do-end to a multiline {}, or vice versa? Naeh,
                // they can do this in two steps; it's not common
                fixList.add(new ConvertTypeFix(info, node, convertFromBrace, !convertFromBrace, false, false));
                if (sameLine || (!sameLine && offerCollapse)) {
                    fixList.add(new ConvertTypeFix(info, node, false, false, sameLine, !sameLine));
                }
                Description desc = new Description(this, getDisplayName(), info.getFileObject(), range, fixList, 500);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getId() {
        return "Convert_Blocktype"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConvertBlockType.class, "ConvertBlockType");
    }

    public String getDescription() {
        return NbBundle.getMessage(ConvertBlockType.class, "ConvertBlockTypeDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean showInTasklist() {
        return false;
    }

    private static class ConvertTypeFix implements PreviewableFix {

        private final CompilationInfo info;
        private final boolean convertToDo;
        private final boolean convertToBrace;
        private final Node node;
        private final boolean expand;
        private final boolean collapse;

        ConvertTypeFix(CompilationInfo info, Node node, 
                boolean convertToDo, boolean convertToBrace,
                boolean expand, boolean collapse) {
            this.info = info;
            this.node = node;
            this.convertToDo = convertToDo;
            this.convertToBrace = convertToBrace;
            this.expand = expand;
            this.collapse = collapse;
        }

        public String getDescription() {
            String key;
            if (convertToDo) {
                if (expand) {
                    key = "ConvertBraceToDoMulti"; // NOI18N
                } else if (collapse) {
                    key = "ConvertBraceToDoSingle"; // NOI18N
                } else {
                    key = "ConvertBraceToDo"; // NOI18N
                }
            } else if (convertToBrace) {
                if (expand) {
                    key = "ConvertDoToBraceMulti"; // NOI18N
                } else if (collapse) {
                    key = "ConvertDoToBraceSingle"; // NOI18N
                } else {
                    key = "ConvertDoToBrace"; // NOI18N
                }
            } else {
                if (expand) {
                    key = "ChangeBlockToMulti"; // NOI18N
                } else {
                    assert collapse;
                    key = "ChangeBlockToSingle"; // NOI18N
                }
            }
            return NbBundle.getMessage(ConvertBlockType.class, key);
        }


        public boolean canPreview() {
            return true;
        }

        public void implement() throws Exception {
            getEditList().apply();
        }
        
        public EditList getEditList() throws Exception {
            BaseDocument doc = (BaseDocument) info.getDocument();
            EditList edits = new EditList(doc);

            ISourcePosition pos = node.getPosition();
            int startOffset = pos.getStartOffset();
            int originalEnd = pos.getEndOffset();
            int endOffset;
            if (convertToDo) {
                endOffset = originalEnd - 1;
            } else if (convertToBrace) {
                endOffset = originalEnd - 3;
            } else {
                endOffset = originalEnd;
            }
            if (startOffset > doc.getLength() - 1 || endOffset > doc.getLength()) {
                return edits;
            }

            if (convertToDo) {
                if (doc.getText(startOffset, 1).charAt(0) == '{' && doc.getText(endOffset, 1).charAt(0) == '}') {
                    String end;
                    if (endOffset > 0 && !Character.isWhitespace(doc.getText(endOffset - 1, 1).charAt(0))) {
                        end = " end"; // NOI18N
                    } else {
                        end = "end"; // NOI18N
                    }
                    edits.replace(endOffset, 1, end, false, 0); // NOI18N

                    boolean spaceBefore = true;
                    boolean spaceAfter = true;
                    if (startOffset > 0) {
                        String s = doc.getText(startOffset - 1, 3);
                        spaceBefore = Character.isWhitespace(s.charAt(0));
                        spaceAfter = Character.isWhitespace(s.charAt(2));
                    }
                    String insert = "do";
                    if (!spaceAfter) {
                        insert = insert + " ";
                    }
                    if (!spaceBefore) {
                        insert = " " + insert;
                    }
                    edits.replace(startOffset, 1, insert, false, 1); // NOI18N

                    if (expand) {
                        expand(edits, doc, node, startOffset, originalEnd);
                    } else if (collapse) {
                        collapse(edits, doc, node, startOffset, originalEnd);
                    }
                }
            } else if (convertToBrace) {
                if (doc.getText(startOffset, 2).equals("do") && endOffset <= doc.getLength() - 3 && // NOI18N
                        doc.getText(endOffset, 3).equals("end")) { // NOI18N
                    // TODO - make sure there is whitespace next to these tokens!!!
                    // They are optional around {} but not around do/end!
                    AstPath path = new AstPath(AstUtilities.getRoot(info), node);
                    assert path.leaf() == node;
                    boolean parenIsNecessary = isArgParenNecessary(path, doc);

                    edits.replace(endOffset, 3, "}", false, 0); // NOI18N
                    edits.replace(startOffset, 2, "{", false, 0); // NOI18N

                    if (parenIsNecessary) {
                        // Insert parentheses
                        assert AstUtilities.isCall(path.leafParent());
                        OffsetRange range = AstUtilities.getCallRange(path.leafParent());
                        int insertPos = range.getEnd();
                        // Check if I should remove a space; e.g. replace "foo arg" with "foo(arg"
                        if (Character.isWhitespace(doc.getText(insertPos, 1).charAt(0))) {
                            edits.replace(insertPos, 1, "(", false, 1); // NOI18N
                        } else {
                            edits.replace(insertPos, 0, "(", false, 1); // NOI18N
                        }

                        // Insert )
                        edits.replace(startOffset-1, 0, ")", false, 2); // NOI18N

                        if (!Character.isWhitespace(doc.getText(startOffset-1, 1).charAt(0))) {
                            edits.replace(startOffset-1, 0, " ", false, 3); // NOI18N
                        }
                    }

                    if (expand) {
                        expand(edits, doc, node, startOffset, originalEnd);
                    } else if (collapse) {
                        collapse(edits, doc, node, startOffset, originalEnd);
                    }
                }
            } else {
                assert collapse || expand;

                if (expand) {
                    expand(edits, doc, node, startOffset, endOffset);
                } else {
                    collapse(edits, doc, node, startOffset, endOffset);
                }
            }

            return edits;
        }
        
        /** JRuby sometimes has wrong AST offsets. For example, for 
         * this IterNode
         * sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
         * the NewlineNode inside the iter will be here: a1^[0] instead of ^a1[0].
         * To work around this problem, look at the left most children of a NewlineNode
         * and find the TRUE starting range of the newline node.
         * @todo File JRuby issue
         */
        private int findRealStart(Node node) {
            int min = Integer.MAX_VALUE;
            while (true) {
                int start = node.getPosition().getStartOffset();
                if (node.nodeId == NodeTypes.YIELDNODE) {
                    // Yieldnodes sometimes have the wrong offsets - see testHintFix19
                    // as well as highlightExitPoints in OccurrencesFinder for more
                    try {
                        OffsetRange range = AstUtilities.getYieldNodeRange((YieldNode)node, 
                                (BaseDocument)info.getDocument());
                        if (range != OffsetRange.NONE) {
                            start = range.getStart();
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }

                if (start < min) {
                    min = start;
                }

                @SuppressWarnings(value = "unchecked")
                List<Node> list = node.childNodes();

                if (list != null && list.size() > 0) {
                    node = list.get(0);
                } else {
                    return min;
                }
            }
        }

        private void findLineBreaks(Node node, Set<Integer> offsets) {
            if (node.nodeId == NodeTypes.NEWLINENODE) {
                // Doesn't work, need above workaround
                //int start = node.getPosition().getStartOffset();
                int start = findRealStart(node);
                offsets.add(start);
            }

            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.nodeId == NodeTypes.EVSTRNODE) {
                    // Don't linebreak inside a #{} expression
                    continue;
                }
                findLineBreaks(child, offsets);
            }
        }

        /** NOTE - document should be under atomic lock when this is called */
        private void expand(EditList edits, BaseDocument doc, Node node, int startOffset, int endOffset) {
            assert endOffset <= doc.getLength();

            // Look through the document and find the statement separators (;);
            // at these locations I'll replace the ; with a newline and then
            // apply a formatter
            Set<Integer> offsetSet = new HashSet<Integer>();
            findLineBreaks(node, offsetSet);

            // Add in ; replacements
            TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc, endOffset);
            if (ts != null) {
                // Traverse sequence in reverse order such that my offset list is in decreasing order
                ts.move(endOffset);
                while (ts.movePrevious() && ts.offset() > startOffset) {
                    Token<? extends RubyTokenId> token = ts.token();
                    TokenId id = token.id();

                    if (id == RubyTokenId.IDENTIFIER && ";".equals(token.text().toString())) { // NOI18N
                        //offsetSet.add(ts.offset());
                    } else if (id == RubyTokenId.END || id == RubyTokenId.RBRACE) {
                        offsetSet.add(ts.offset());
                    }
                }
            }

            List<Integer> offsets = new ArrayList<Integer>(offsetSet);
            Collections.sort(offsets);
            // Ensure that we go in high to lower order such that I edit the
            // document from bottom to top (so offsets don't have to be adjusted
            // to account for our own edits along the way)
            Collections.reverse(offsets);

            if (offsets.size() > 0) {
                // TODO: Create a ModificationResult here and process it
                // The following is the WRONG way to do it...
                // I've gotta use a ModificationResult instead!
                try {
                    // Process offsets from back to front such that I can
                    // modify the document without worrying that the other offsets
                    // need to be adjusted
                    int prev = -1;
                    int added = 0;
                    for (int offset : offsets) {
                        // We might get some dupes since we add offsets from both
                        // the AST newline nodes and semicolons discovered in the lexical token hierarchy
                        if (offset == prev) {
                            continue;
                        }
                        prev = offset;
                        
                        // Back up over any whitespace
                        int whitespaces = 0;
                        for (int i = 1; i < 5 && offset-i > 0; i++) {
                            char c = doc.getText(offset-i, 1).charAt(0);
                            if (Character.isWhitespace(c)) {
                                whitespaces++;
                            } else {
                                break;
                            }
                        }

                        if (whitespaces > 0) {
                            edits.replace(offset-whitespaces, whitespaces, "\n", false, 4); // NOI18N
                        } else {
                            edits.replace(offset, 0, "\n", false, 4); // NOI18N
                        }
                        added++;
                    }

                    // Remove trailing semicolons
                    for (int offset : offsets) {
                        char c = doc.getText(offset-1, 1).charAt(0);
                        if (c == ';') {
                            edits.replace(offset-1, 1, null, false, 5);
                        } else if (Character.isWhitespace(c)) {
                            c = doc.getText(offset-2, 1).charAt(0);
                            if (c == ';') {
                                edits.replace(offset-2, 1, null, false, 5);
                            }
                        }
                    }
                    int newEnd = endOffset + added;

                    // Remove trailing whitespace
                    // TODO

                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
            edits.format();
        }

        private void collapse(EditList edits, BaseDocument doc, Node node, int startOffset, int endOffset) {
            assert endOffset <= doc.getLength();

            // Look through the document and find the statement separators (;);
            // at these locations I'll replace the ; with a newline and then
            // apply a formatter
            Set<Integer> offsetSet = new HashSet<Integer>();
            findLineBreaks(node, offsetSet);

            Token<? extends TokenId> t = LexUtilities.getToken(doc, startOffset);
            TokenId tid = t.id();
            assert tid == RubyTokenId.LBRACE || tid == RubyTokenId.DO;
            boolean isDoBlock = tid == RubyTokenId.DO;

            // Add in ; replacements
            TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc, endOffset);
            if (ts != null) {
                // Traverse sequence in reverse order such that my offset list is in decreasing order
                ts.move(endOffset);
                while (ts.movePrevious() && ts.offset() > startOffset) {
                    Token<? extends RubyTokenId> token = ts.token();
                    TokenId id = token.id();

                    if (id == RubyTokenId.END || id == RubyTokenId.RBRACE) {
                        offsetSet.add(ts.offset());
                    }
                }
            }

            List<Integer> offsets = new ArrayList<Integer>(offsetSet);
            Collections.sort(offsets);
            // Ensure that we go in high to lower order such that I edit the
            // document from bottom to top (so offsets don't have to be adjusted
            // to account for our own edits along the way)
            //Collections.reverse(offsets);
            if (offsets.size() > 0) {
                // TODO: Create a ModificationResult here and process it
                // The following is the WRONG way to do it...
                // I've gotta use a ModificationResult instead!
                try {
                    // Process offsets from back to front such that I can
                    // modify the document without worrying that the other offsets
                    // need to be adjusted
                    int prev = -1;
                    //int posDelta; // Amount to add to offsets to account for our
                    for (int i = offsets.size() - 1; i >= 0; i--) {
                        int offset = offsets.get(i);
                        // We might get some dupes since we add offsets from both
                        // the AST newline nodes and semicolons discovered in the lexical token hierarchy
                        if (offset == prev) {
                            continue;
                        }
                        prev = offset;
                        int prevOffset = i > 0 ? offsets.get(i - 1) : 0;

                        int segmentOffset = offset;
                        // TODO - use an editor-finder which can do this efficiently
                        // See also DocumentUtilities.getText() which can do it efficiently
                        int s = segmentOffset;
                        while (s > prevOffset) {
                            s--;
                            char c = doc.getText(s, 1).charAt(0);
                            if (Character.isWhitespace(c)) {
                                segmentOffset = s;
                            } else {
                                break;
                            }
                        }
                        int segmentLength = offset - segmentOffset;
                        s = offset - 1;
                        while (s < doc.getLength()) {
                            s++;
                            char c = doc.getText(s, 1).charAt(0);
                            if (Character.isWhitespace(c)) {
                                segmentLength++;
                            } else {
                                break;
                            }
                        }

                        // Collapse all whitespace around this offset and replace with a single "; "
                        char prevChar = '?';
                        if (segmentOffset > 0) {
                            prevChar = doc.getText(segmentOffset-1, 1).charAt(0);
                        }
                        if (prevChar == '|' || (isDoBlock && (segmentOffset <= startOffset + 3) || (!isDoBlock && (segmentOffset <= startOffset + 1)))) {
                            edits.replace(segmentOffset, segmentLength, " ", false, 4);
                        } else {
                            // Don't insert semicolons before "end" or around parens in "if (true)" etc.
                            boolean skipSemicolon = false;
                            //if (segmentOffset > 0) {
                            //    Token tkr = LexUtilities.getToken(doc, segmentOffset-1);
                            //    if (tkr != null && tkr.id() == RubyTokenId.RPAREN) {
                            //        skipSemicolon = true;
                            //    }
                            //}
                            TokenSequence<? extends TokenId> rts = LexUtilities.getRubyTokenSequence(doc, segmentOffset);
                            rts.move(segmentOffset);
                            while (rts.moveNext()) {
                                Token tk = rts.token();
                                TokenId tkid = tk.id();
                                if (tkid == RubyTokenId.END || tkid == RubyTokenId.RBRACE ||
                                        tkid == RubyTokenId.LPAREN) {
                                    skipSemicolon = true;
                                    break;
                                } else if (tkid != RubyTokenId.WHITESPACE) {
                                    break;
                                }
                            }
                            if (skipSemicolon) {
                              edits.replace(segmentOffset, segmentLength, " ", false, 4);
                            } else {
                              edits.replace(segmentOffset, segmentLength, "; ", false, 4);
                            }
                        }
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
            edits.format();
        }

        /** Determine whether parentheses are necessary around the call
         * corresponding to a block call.
         * For example, in 
         * <pre>
         *  b.create_menu :name => 'default_menu' do |d| ...
         * </pre>
         * parens are necessary if you want to switch to a brace block.
         */
        private boolean isArgParenNecessary(AstPath path, BaseDocument doc) throws BadLocationException {
            // Look at the surrounding CallNode and see if it has arguments.
            // If so, see if it has parens. If not, return true.
            assert path.leaf().nodeId == NodeTypes.ITERNODE;
            Node n = path.leafParent();
            if (n != null && AstUtilities.isCall(n) && n instanceof IArgumentNode && 
                    ((IArgumentNode)n).getArgsNode() != null) {
                // Yes, call has args - check parens
                int end = node.getPosition().getStartOffset(); // Start of do/{ - end of args
                for (int i = end-1; i >= 0 && i < doc.getLength(); i--) {
                    // XXX Use a more performant document content iterator!
                    char c = doc.getText(i, 1).charAt(0);
                    if (Character.isWhitespace(c)) {
                        continue;
                    }
                    if (c == ')') {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            
            return false;
        }

        public boolean isSafe() {
            // Different precedence rules apply for do and {}
            return !convertToBrace && !convertToDo;
        }

        public boolean isInteractive() {
            return false;
        }
    }
}
