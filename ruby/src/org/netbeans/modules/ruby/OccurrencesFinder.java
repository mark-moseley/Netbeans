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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;

import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.GlobalAsgnNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.ReturnNode;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.YieldNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.ColoringAttributes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.util.Exceptions;


/**
 * Walk through the JRuby AST and find occurrences of symbols related to the symbol under the cursor
 *
 * @todo Highlight exit points: break (if exits method?), uncaught exceptions, throws, etc.
 *   It would be cool if I can highlight exits out of some types of blocks too, like for and while
 *   loops where I highlight retry, break, redo(?), return, uncaught throws.
 * @todo Highlight symbol nodes. If you have a "class Foo" and refer to :Foo then class Foo should
 *   be marked.
 *
 * @author Tor Norbye
 */
public class OccurrencesFinder implements org.netbeans.api.gsf.OccurrencesFinder {
    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences;

    /** When true, don't match alias nodes as reads. Used during traversal of the AST. */
    private boolean ignoreAlias;

    public OccurrencesFinder() {
    }

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurrences;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public final synchronized void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo info) {
        resume();

        if (isCancelled()) {
            return;
        }

        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights =
            new HashMap<OffsetRange, ColoringAttributes>(100);

        RubyParseResult rpr = (RubyParseResult)info.getParserResult();

        AstPath path = new AstPath(root, caretPosition);
        Node closest = path.leaf();

        // When we sanitize the line around the caret, occurrences
        // highlighting can get really ugly
        OffsetRange blankRange = rpr.getSanitizedRange();

        if (blankRange.containsInclusive(caretPosition)) {
            closest = null;
        }

        // JRuby sometimes gives me some "weird" sections. For example,
        // if you have
        //    obj.|
        //
        //    Scanf
        // rather than give a parse error on obj, it marks the whole region from
        // . to the end of Scanf as a CallNode, which is a weird highlight.
        // We don't want occurrences highlights that span lines.
        if (closest != null) {
            ISourcePosition pos = closest.getPosition();

            try {
                BaseDocument doc = (BaseDocument)info.getDocument();

                int length = doc.getLength();
                int startPos = pos.getStartOffset();
                int endPos = pos.getEndOffset();

                // If the buffer was just modified where a lot of text was deleted,
                // the parse tree positions could be pointing outside the valid range
                if (startPos > length) {
                    startPos = length;
                }

                if (endPos > length) {
                    endPos = length;
                }

                if (Utilities.getRowStart(doc, startPos) != Utilities.getRowStart(doc, endPos)) {
                    // One special case I care about: highlighting method exit points. In
                    // this case, the full def node is selected, which typically spans
                    // lines. This should trigger if you put the caret on the method definition
                    // line, unless it's in a comment there.
                    Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, caretPosition);

                    if (((token != null) && (token.id() != RubyTokenId.LINE_COMMENT)) &&
                            (closest instanceof MethodDefNode) &&
                            (Utilities.getRowStart(doc, pos.getStartOffset()) == Utilities.getRowStart(
                                doc, caretPosition))) {
                        // Highlight exit points
                        highlightExits((MethodDefNode)closest, highlights, info);

                        // Fall through and set closest to null such that I don't do other highlighting
                    }

                    // Some nodes may span multiple lines, but the range we care about is only
                    // on a single line because we're pulling out the lvalue - for example,
                    // a method call may span multiple lines because of a long parameter list,
                    // but we only highlight the methodname itself
                    if (!(closest instanceof LocalAsgnNode || closest instanceof FCallNode ||
                            closest instanceof DAsgnNode || closest instanceof InstAsgnNode ||
                            closest instanceof ClassVarDeclNode ||
                            closest instanceof ClassVarAsgnNode ||
                            closest instanceof GlobalAsgnNode || closest instanceof ConstDeclNode)) {
                        closest = null;
                    }
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        if (closest != null) {
            if (closest instanceof LocalVarNode || closest instanceof LocalAsgnNode) {
                // A local variable read or a parameter read, or an assignment to one of these
                String name = ((INameNode)closest).getName();
                Node method = AstUtilities.findLocalScope(closest, path);

                highlightLocal(method, name, highlights);
            } else if (closest instanceof DAsgnNode) {
                // A dynamic variable read or assignment
                String name = ((INameNode)closest).getName();
                Node block = AstUtilities.findLocalScope(closest, path);

                highlightDynamnic(block, name, highlights);
            } else if (closest instanceof DVarNode) {
                // A dynamic variable read or assignment
                String name = ((DVarNode)closest).getName(); // Does not implement INameNode
                Node block = AstUtilities.findLocalScope(closest, path);

                highlightDynamnic(block, name, highlights);
            } else if (closest instanceof InstAsgnNode) {
                // A field assignment
                String name = ((INameNode)closest).getName();
                highlightInstance(root, name, highlights);
            } else if (closest instanceof InstVarNode) {
                // A field variable read
                highlightInstance(root, ((INameNode)closest).getName(), highlights);
            } else if (closest instanceof ClassVarDeclNode || closest instanceof ClassVarAsgnNode) {
                // A classvar assignment
                String name = ((INameNode)closest).getName();
                highlightClassVar(root, name, highlights);
            } else if (closest instanceof ClassVarNode) {
                // A xclass variable read
                highlightClassVar(root, ((ClassVarNode)closest).getName(), highlights);
            } else if (closest instanceof GlobalVarNode) {
                // A global variable read
                String name = ((GlobalVarNode)closest).getName(); // GlobalVarNode does not implement INameNode
                highlightGlobal(root, name, highlights);
            } else if (closest instanceof GlobalAsgnNode) {
                // A global variable assignment
                String name = ((INameNode)closest).getName();
                highlightGlobal(root, name, highlights);
            } else if (closest instanceof FCallNode || closest instanceof VCallNode ||
                    closest instanceof CallNode) {
                // A method call
                String name = ((INameNode)closest).getName();

                if ("raise".equals(name) || "fail".equals(name)) { // NOI18N

                    Node def = AstUtilities.findMethod(path);

                    if (def instanceof MethodDefNode) {
                        highlightExits((MethodDefNode)def, highlights, info);
                    }
                } else {
                    // I shouldn't just highlight matches that match my call arity; I want
                    // to highlight all other calls that match the same set of methods.
                    Arity callArity = Arity.getCallArity(closest);
                    List<Arity> defArities = new ArrayList<Arity>();
                    findDefArities(defArities, root, name, callArity);

                    if (defArities.size() == 0) {
                        // No matching declarations; just use this call
                        defArities.add(callArity);
                    }

                    // Try placing the caret on a "?" - you'll see a method call to [].
                    // While it's a method call it's not what the user thinks of as one, so suppress it.
                    if (!name.equals("[]")) {
                        highlightMethod(root, name, defArities, highlights);
                    }
                }
            } else if (closest instanceof YieldNode || closest instanceof ReturnNode) {
                Node def = AstUtilities.findMethod(path);

                if (def instanceof MethodDefNode) {
                    highlightExits((MethodDefNode)def, highlights, info);
                }
            } else if (closest instanceof MethodDefNode) {
                // A method definition. Only highlight if the caret is on the
                // actual name, since otherwise just placing the caret on a blank
                // line in a method will cause it to highlight.
                OffsetRange range = AstUtilities.getFunctionNameRange(root);

                if (range.containsInclusive(caretPosition)) {
                    String name = ((MethodDefNode)closest).getName();
                    highlightMethod(root, name,
                        Collections.singletonList(Arity.getDefArity(closest)), highlights);
                }
            } else if (closest instanceof Colon2Node) {
                // A Class definition
                highlights.put(AstUtilities.getRange(closest), ColoringAttributes.MARK_OCCURRENCES);

                highlightClass(root, ((INameNode)closest).getName(), highlights);

                // TODO: alias nodes
            } else if (closest instanceof ConstNode || closest instanceof ConstDeclNode) {
                // POSSIBLY a class usage.
                //highlights.put(AstUtilities.getRange(closest), ColoringAttributes.MARK_OCCURRENCES);
                highlightClass(root, ((INameNode)closest).getName(), highlights);
            } else if (closest instanceof SymbolNode) {
                // TODO - what about Symbols for other things than fields?
                String name = ((INameNode)closest).getName();
                highlightInstance(root, "@" + name, highlights);
                highlightClassVar(root, "@@" + name, highlights);
                highlightMethod(root, name, Collections.singletonList(Arity.UNKNOWN), highlights);
                highlightClass(root, name, highlights);
            } else if (closest instanceof AliasNode) {
                AliasNode an = (AliasNode)closest;

                // TODO - determine if the click is over the new name or the old name
                String newName = an.getNewName();

                // XXX I don't know where the old and new names are since the user COULD
                // have used more than one whitespace character for separation. For now I'll
                // just have to assume it's the normal case with one space:  alias new old. 
                // I -could- use the getPosition.getEndOffset() to see if this looks like it's
                // the case (e.g. node length != "alias ".length + old.length+new.length+1).
                // In this case I could go peeking in the source buffer to see where the
                // spaces are - between alias and the first word or between old and new. XXX.
                int newLength = newName.length();
                int aliasPos = an.getPosition().getStartOffset();
                String name = null;

                if (caretPosition > (aliasPos + 6)) { // 6: "alias ".length()

                    if (caretPosition > (aliasPos + 6 + newLength)) {
                        OffsetRange range = AstUtilities.getAliasOldRange(an);
                        highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                        name = an.getOldName();
                    } else {
                        OffsetRange range = AstUtilities.getAliasNewRange(an);
                        highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                        name = an.getNewName();
                    }
                }

                if (name != null) {
                    // It's over the old word: this counts as a usage.
                    // The problem is that we don't know if it's a local, a dynamic, an instance
                    // variable, etc. (The $ and @ parts are not included in the alias statement).
                    // First see if it's a local variable.
                    int count = highlights.size();
                    Node method = AstUtilities.findLocalScope(closest, path);

                    // We don't want alias nodes being added while searching for locals since that
                    // will make it look like a local was found (since the set will grow)
                    ignoreAlias = true;

                    try {
                        highlightLocal(method, name, highlights);

                        if (highlights.size() == count) {
                            // Didn't find locals... try dynvars
                            Node block = AstUtilities.findLocalScope(closest, path);

                            highlightDynamnic(block, name, highlights);

                            if (highlights.size() == count) {
                                // Didn't find locals... try methods
                                highlightMethod(root, name,
                                    Collections.singletonList(Arity.UNKNOWN), highlights);

                                if (highlights.size() == count) {
                                    // Didn't find methods... try instance fields
                                    highlightInstance(root, name, highlights);

                                    if (highlights.size() == count) {
                                        // Didn't find instance methods, try globals
                                        highlightGlobal(root, name, highlights);

                                        if (highlights.size() == count) {
                                            // Didn't find globals, try classes
                                            highlightClass(root, name, highlights);

                                            if (highlights.size() == count) {
                                                // Now try classvars
                                                highlightClassVar(root, name, highlights);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        ignoreAlias = false;
                    }
                }
            } else if (closest instanceof ArgumentNode) {
                // A method name (if under a DefnNode or DefsNode) or a parameter (if indirectly under an ArgsNode)
                String name = ((ArgumentNode)closest).getName(); // ArgumentNode doesn't implement INameNode

                Node parent = path.leafParent();

                if (parent != null) {
                    if (parent instanceof MethodDefNode) {
                        //highlightMethod(root, name,
                        //    Collections.singletonList(Arity.getDefArity(parent)), highlights);
                        highlightExits((MethodDefNode)parent, highlights, info);
                    } else {
                        // Parameter (check to see if its under ArgumentNode)
                        Node method = AstUtilities.findLocalScope(closest, path);

                        highlightLocal(method, name, highlights);
                    }
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            this.occurrences = highlights;
        } else {
            this.occurrences = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightExits(MethodDefNode node,
        Map<OffsetRange, ColoringAttributes> highlights, CompilationInfo info) {
        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightExitPoints(child, highlights, info);
        }

        // TODO: Find the last statement, and highlight it.
        // Be careful not to highlight the entire statement (which could be a giant if
        // statement spanning the whole screen); just pick the first line.
        Node last = null;

        for (int i = list.size() - 1; i >= 0; i--) {
            last = list.get(i);

            if (last instanceof ArgsNode || last instanceof ArgumentNode) {
                // Done - no valid statement
                return;
            }

            if (last instanceof ListNode) {
                last = (Node)last.childNodes().get(last.childNodes().size() - 1);
            }

            if (last instanceof NewlineNode && (last.childNodes().size() > 0)) {
                last = (Node)last.childNodes().get(last.childNodes().size() - 1);

                break;
            }

            break;
        }

        if (last != null) {
            try {
                BaseDocument doc = (BaseDocument)info.getDocument();
                ISourcePosition pos = last.getPosition();

                if (Utilities.getRowStart(doc, pos.getStartOffset()) != Utilities.getRowStart(doc,
                            pos.getEndOffset())) {
                    // Highlight the first line - where the nonwhitespace is
                    int begin = Utilities.getRowFirstNonWhite(doc, pos.getStartOffset());
                    int end = Utilities.getRowLastNonWhite(doc, pos.getStartOffset());

                    if ((begin != -1) && (end != -1)) {
                        OffsetRange range = new OffsetRange(begin, end + 1);
                        highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                    }
                } else {
                    OffsetRange range = AstUtilities.getRange(last);
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightExitPoints(Node node, Map<OffsetRange, ColoringAttributes> highlights,
        CompilationInfo info) {
        if (node instanceof ReturnNode) {
            OffsetRange range = AstUtilities.getRange(node);
            try {
                BaseDocument doc = (BaseDocument)info.getDocument();
                int lineStart = Utilities.getRowStart(doc, range.getStart());
                int endLineStart = Utilities.getRowStart(doc, range.getEnd());
                if (lineStart != endLineStart) {
                    range = new OffsetRange(range.getStart(), Utilities.getRowEnd(doc, range.getStart()));
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
        } else if (node instanceof YieldNode) {
            /* Yield in the following code has the wrong offsets in JRuby
              if component.size == 1
                yield component.first
              else
                raise Cyclic.new("topological sort failed: #{component.inspect}")
              end
             */
            try {
                BaseDocument doc = (BaseDocument)info.getDocument();
                ISourcePosition pos = node.getPosition();

                int offset = pos.getStartOffset();
                int lineStart = Utilities.getRowStart(doc, offset);
                int lineLength = Utilities.getRowEnd(doc, offset) - lineStart;
                String text = doc.getText(lineStart, lineLength);
                int index = text.indexOf("yield"); // NOI18N

                if ((index == -1) || (text.charAt(offset - lineStart) == 'y')) {
                    // The positions might be correct
                    OffsetRange range = AstUtilities.getRange(node);
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                } else {
                    // Correct position
                    OffsetRange range =
                        new OffsetRange(lineStart + index, lineStart + index + "yield".length()); // NOI18N
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        } else if (node instanceof MethodDefNode || node instanceof ClassNode ||
                node instanceof SClassNode || node instanceof ModuleNode) {
            // Don't go into sub methods, classes, etc
            return;
        } else if (node instanceof FCallNode) {
            FCallNode fc = (FCallNode)node;

            if ("fail".equals(fc.getName()) || "raise".equals(fc.getName())) {
                OffsetRange range = AstUtilities.getCallRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightExitPoints(child, highlights, info);
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightLocal(Node node, String name,
        Map<OffsetRange, ColoringAttributes> highlights) {
        if (node instanceof LocalVarNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof LocalAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getLValueRange((LocalAsgnNode)node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof ArgsNode) {
            ArgsNode an = (ArgsNode)node;

            if (an.getArgsCount() > 0) {
                List<Node> args = (List<Node>)an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = (List<Node>)arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2 instanceof ArgumentNode) {
                                if (((ArgumentNode)arg2).getName().equals(name)) {
                                    OffsetRange range = AstUtilities.getRange(arg2);
                                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                                }
                            } else if (arg2 instanceof LocalAsgnNode) {
                                if (((LocalAsgnNode)arg2).getName().equals(name)) {
                                    OffsetRange range = AstUtilities.getRange(arg2);
                                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                                }
                            }
                        }
                    }
                }
            }

            // Rest args
            if (an.getRestArgNode() != null) {
                ArgumentNode bn = an.getRestArgNode();

                if (bn.getName().equals(name)) {
                    OffsetRange range = AstUtilities.getRange(bn);
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            }

            if (an.getBlockArgNode() != null) {
                BlockArgNode bn = an.getBlockArgNode();

                if (bn.getName().equals(name)) {
                    OffsetRange range = AstUtilities.getRange(bn);
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            AliasNode an = (AliasNode)node;

            if (an.getNewName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasNewRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            } else if (an.getOldName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasOldRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof SymbolNode) {
            if (((INameNode)node).getName().equals(name)) { // NOI18N

                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightLocal(child, name, highlights);
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightDynamnic(Node node, String name,
        Map<OffsetRange, ColoringAttributes> highlights) {
        if (node instanceof DVarNode) {
            if (((DVarNode)node).getName().equals(name)) { // Does not implement INameNode

                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof DAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getLValueRange((DAsgnNode)node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            AliasNode an = (AliasNode)node;

            if (an.getNewName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasNewRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            } else if (an.getOldName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasOldRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightDynamnic(child, name, highlights);
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightInstance(Node node, String name,
        Map<OffsetRange, ColoringAttributes> highlights) {
        if (node instanceof InstVarNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof InstAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getLValueRange((InstAsgnNode)node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            AliasNode an = (AliasNode)node;

            if (an.getNewName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasNewRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            } else if (an.getOldName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasOldRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (AstUtilities.isAttr(node)) {
            // TODO: Compute the symbols and check for equality
            // attr_reader, attr_accessor, attr_writer
            SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);

            for (int i = 0; i < symbols.length; i++) {
                if (name.equals("@" + symbols[i].getName())) {
                    OffsetRange range = AstUtilities.getRange(symbols[i]);
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        } else if (node instanceof SymbolNode) {
            if (("@" + ((INameNode)node).getName()).equals(name)) { // NOI18N

                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightInstance(child, name, highlights);
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightClassVar(Node node, String name,
        Map<OffsetRange, ColoringAttributes> highlights) {
        if (node instanceof ClassVarNode) {
            if (((ClassVarNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof ClassVarDeclNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getLValueRange((ClassVarDeclNode)node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof ClassVarAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getLValueRange((ClassVarAsgnNode)node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            AliasNode an = (AliasNode)node;

            if (an.getNewName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasNewRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            } else if (an.getOldName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasOldRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof SymbolNode) {
            if (("@@" + ((INameNode)node).getName()).equals(name)) { // NOI18N

                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }

            // TODO - are there attr writers for class vars?
            //        } else if (AstUtilities.isAttrReader(node) || AstUtilities.isAttrWriter(node)) {
            //            // TODO: Compute the symbols and check for equality
            //            // attr_reader, attr_accessor, attr_writer
            //            SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);
            //
            //            for (int i = 0; i < symbols.length; i++) {
            //                if (name.equals("@@" + symbols[i].getName())) {
            //                    OffsetRange range = AstUtilities.getRange(symbols[i]);
            //                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            //                }
            //            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightClassVar(child, name, highlights);
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightGlobal(Node node, String name,
        Map<OffsetRange, ColoringAttributes> highlights) {
        if (node instanceof GlobalVarNode) {
            //if (((INameNode)node).getName().equals(name)) { // GlobalVarNode does not implement INameNode
            if (((GlobalVarNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof GlobalAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getLValueRange((GlobalAsgnNode)node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            AliasNode an = (AliasNode)node;

            if (an.getNewName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasNewRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            } else if (an.getOldName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasOldRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof SymbolNode) {
            if (("$" + ((INameNode)node).getName()).equals(name)) { // NOI18N

                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightGlobal(child, name, highlights);
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightMethod(Node node, String name, List<Arity> arities,
        Map<OffsetRange, ColoringAttributes> highlights) {
        // Recursively search for methods or method calls that match the name and arity
        if (node instanceof MethodDefNode && ((MethodDefNode)node).getName().equals(name)) {
            Arity defArity = Arity.getDefArity(node);

            for (Arity arity : arities) {
                if (Arity.matches(arity, defArity)) {
                    OffsetRange range = AstUtilities.getFunctionNameRange(node);
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);

                    break;
                }
            }
        } else if ((node instanceof FCallNode || node instanceof CallNode ||
                node instanceof VCallNode) && ((INameNode)node).getName().equals(name)) {
            Arity callArity = Arity.getCallArity(node);

            for (Arity arity : arities) {
                if (Arity.matches(callArity, arity)) {
                    OffsetRange range = AstUtilities.getCallRange(node);
                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            AliasNode an = (AliasNode)node;

            if (an.getNewName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasNewRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            } else if (an.getOldName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasOldRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof SymbolNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightMethod(child, name, arities, highlights);
        }
    }

    /** Find the definition arity that matches a given call arity */
    @SuppressWarnings("unchecked")
    private void findDefArities(List<Arity> defArities, Node node, String name, Arity callArity) {
        // Recursively search for methods or method calls that match the name and arity
        if (node instanceof MethodDefNode && ((MethodDefNode)node).getName().equals(name)) {
            Arity defArity = Arity.getDefArity(node);

            if (Arity.matches(callArity, defArity)) {
                defArities.add(defArity);
            }
        }

        List<Node> list = node.childNodes();

        Arity combinedArity = null;

        for (Node child : list) {
            findDefArities(defArities, child, name, callArity);
        }
    }

    @SuppressWarnings("unchecked")
    private void highlightClass(Node node, String name,
        Map<OffsetRange, ColoringAttributes> highlights) {
        if (node instanceof ConstNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof ConstDeclNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getLValueRange((ConstDeclNode)node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof Colon2Node) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            AliasNode an = (AliasNode)node;

            if (an.getNewName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasNewRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            } else if (an.getOldName().equals(name)) {
                OffsetRange range = AstUtilities.getAliasOldRange(an);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (node instanceof SymbolNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            highlightClass(child, name, highlights);
        }
    }

    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }
}
