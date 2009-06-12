/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import static org.netbeans.modules.java.source.save.PositionEstimator.NOPOS;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
 * @todo documentation
 */
public final class CommentCollector {
    private static CommentCollector instance = null;

    public static CommentCollector getInstance() {
        synchronized (CommentsCollection.class) {
            if (instance == null) {
                instance = new CommentCollector();
            }
        }
        return instance;
    }

    private CommentCollector() {
    }


    public void collect(WorkingCopy copy) throws IOException {
        JCTree.JCCompilationUnit unit = (JCTree.JCCompilationUnit) copy.getCompilationUnit();
        TokenSequence<JavaTokenId> seq = ((SourceFileObject) unit.getSourceFile()).getTokenHierarchy().tokenSequence(JavaTokenId.language());
        collect(seq, copy);
    }

    public void collect(TokenSequence<JavaTokenId> ts, CompilationInfo ci) {
        CommentHandler ch = CommentHandlerService.instance(ci.impl.getJavacTask().getContext());
        TreeUtilities tu = new TreeUtilities(ci);
        JCTree.JCCompilationUnit cu = (JCTree.JCCompilationUnit) ci.getCompilationUnit();
        Map<JCTree, Integer> endPositions = cu.endPositions;
        List<? extends Tree> trees = cu.getTypeDecls();
        Tree lastTree = trees.isEmpty() ? ci.getCompilationUnit() : trees.get(0);
        CommentsCollection foundComments = null;
        ts.move(0);
        int newlines = 0;
        while (ts.moveNext()) {
            Token<JavaTokenId> t = ts.token();
            if (t.id() == JavaTokenId.WHITESPACE) {
                newlines += numberOfNL(t);
            } else if (isComment(t.id())) {
                if (foundComments != null) {
                    attachComments(foundComments, lastTree, newlines, ch, endPositions);
                }
                if (t.id() == JavaTokenId.LINE_COMMENT) {
                    newlines += numberOfNL(t);
                }
                foundComments = getCommentsCollection(ts, newlines);
                newlines = 0;
            } else {
                skipEvil(ts);
                Tree tree = getTree(tu, ts);
                if (tree != null && foundComments != null) {
                    int[] bounds = foundComments.getBounds();
                    if (belongsTo(bounds[0], bounds[1], ts) >= 0) {
                        attachComments(foundComments, tree, newlines, ch, endPositions);
                    } else {
                        attachComments(foundComments, lastTree, newlines, ch, endPositions);
                    }
                    lastTree = tree;
                    foundComments = null;
                }
            }

        }
        if (foundComments != null) {
            int start = foundComments.getBounds()[0];
            TreePath path = tu.pathFor(start);
            Tree tree = path == null ? cu : path.getLeaf();
            attachComments(foundComments, tree, newlines, ch, endPositions);
        }
    }

    private void skipEvil(TokenSequence<JavaTokenId> ts) {
        do {
            JavaTokenId id = ts.token().id();
            switch (id) {
                case PUBLIC:
                case PRIVATE:
                case PROTECTED:
                case ABSTRACT:
                case FINAL:
                case STATIC:
                case VOID:
                case VOLATILE:
                case NATIVE:
                case STRICTFP:
                case WHITESPACE:
                    continue;
                default:
                    return;
            }
        } while (ts.moveNext());
    }

    private double belongsTo(int startPos, int endPos, TokenSequence<JavaTokenId> ts) {
        int index = ts.index();
        double result = 0;
        result = getBackwardWeight(startPos, ts, result);
        result = getForwardWeight(endPos, ts, result);
        ts.moveIndex(index);
        ts.moveNext();
        return result;
    }

    private double getForwardWeight(int endPos, TokenSequence<JavaTokenId> ts, double result) {
        ts.move(endPos);
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                int nls = numberOfNL(ts.token());
                result = nls == 0 ? 1 : result + (1 / nls);
            } else if (isComment(ts.token().id())) {
                result = NOPOS;
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private double getBackwardWeight(int startPos, TokenSequence<JavaTokenId> ts, double result) {
        ts.move(startPos);
        while (ts.movePrevious()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                int nls = numberOfNL(ts.token());
                result = nls == 0 ? 0 : result - (1 / nls);
            } else if (isComment(ts.token().id())) {
                result = 0;
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private void attachComments(CommentsCollection foundComments, Tree tree, int nls, CommentHandler ch, Map<JCTree, Integer> endPositions) {
        if (foundComments.isEmpty()) return;
        int[] bounds = getBounds((JCTree) tree, endPositions);
        CommentSet.RelativePosition positioning;
        if (nls == 0) {
            positioning = CommentSet.RelativePosition.INLINE;
        } else {
            positioning = computePositioning(bounds, foundComments);
        }
        CommentSet set = createCommentSet(ch, tree);
        for (Token<JavaTokenId> comment : foundComments) {
            attachComment(positioning, set, comment);
        }
    }

    private void attachComment(CommentSet.RelativePosition positioning, CommentSet set, Token<JavaTokenId> comment) {
        Comment c = Comment.create(getStyle(comment.id()), comment.offset(null),
                getEndPos(comment), NOPOS, getText(comment));
        set.addComment(positioning, c);
    }

    private String getText(Token<JavaTokenId> comment) {
        return String.valueOf(comment.text());
    }

    private int getEndPos(Token<JavaTokenId> comment) {
        return comment.offset(null) + comment.length();
    }

    private Comment.Style getStyle(JavaTokenId id) {
        switch (id) {
            case JAVADOC_COMMENT:
                return Comment.Style.JAVADOC;
            case LINE_COMMENT:
                return Comment.Style.LINE;
            case BLOCK_COMMENT:
                return Comment.Style.BLOCK;
            default:
                return Comment.Style.WHITESPACE;
        }
    }

    private CommentSet.RelativePosition computePositioning(int[] treeBounds, CommentsCollection cc) {
        int[] commentsBounds = cc.getBounds();
        if (commentsBounds[1] < treeBounds[0]) return CommentSet.RelativePosition.PRECEDING;
        if (commentsBounds[0] > treeBounds[1]) return CommentSet.RelativePosition.TRAILING;
        if (commentsBounds[0] > treeBounds[0] && commentsBounds[1] < treeBounds[1])
            return CommentSet.RelativePosition.INNER;
        return CommentSet.RelativePosition.INLINE;
    }

    private int[] getBounds(JCTree tree, Map<JCTree, Integer> endPositions) {
        return new int[]{TreeInfo.getStartPos(tree), TreeInfo.getEndPos(tree, endPositions)};
    }

    private void attachTrailingComments(CommentSet cs, Iterable<Token<JavaTokenId>> foundComments) {
        for (Token<JavaTokenId> comment : foundComments) {
            attachComment(CommentSet.RelativePosition.TRAILING, cs, comment);
        }
    }

    private void attachPrecedingComments(CommentSet cs, Iterable<Token<JavaTokenId>> foundComments) {
        for (Token<JavaTokenId> comment : foundComments) {
            attachComment(CommentSet.RelativePosition.PRECEDING, cs, comment);
        }
    }


    private Tree getTree(TreeUtilities tu, TokenSequence<JavaTokenId> ts) {
        int start = ts.offset();
        TreePath path = tu.pathFor(start);
        if (path != null) {
            return path.getLeaf();
        }
        return null;
    }

    private int numberOfNL(Token<JavaTokenId> t) {
        int count = 0;
        CharSequence charSequence = t.text();
        for (int i = 0; i < charSequence.length(); i++) {
            char a = charSequence.charAt(i);
            if ('\n' == a) {
                count++;
            }
        }
        return count;
    }

    private CommentsCollection getCommentsCollection(TokenSequence<JavaTokenId> ts, int maxTension) {
        CommentsCollection result = new CommentsCollection();
        Token<JavaTokenId> t = ts.token();
        result.add(t);
        int start = ts.offset();
        int end = ts.offset() + ts.token().length();
        while (ts.moveNext()) {
            t = ts.token();
            if (isComment(t.id())) {
                result.add(t);
                start = Math.min(ts.offset(), start);
                end = Math.max(t.length(), end);
            } else if (t.id() == JavaTokenId.WHITESPACE) {
                if (numberOfNL(t) > maxTension) {
                    ts.movePrevious();
                    break;
                }
            } else {
                break;
            }
        }
        result.setBounds(new int[]{start, end});
        return result;
    }

    private CommentSet createCommentSet(CommentHandler ch, Tree lastTree) {
        return ch.getComments(lastTree);
    }

    private boolean isComment(JavaTokenId tid) {
        switch (tid) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
                return true;
            default:
                return false;
        }
    }

    private static class CommentsCollection implements Iterable<Token<JavaTokenId>> {
        private final int[] bounds = {NOPOS, NOPOS};
        private final List<Token<JavaTokenId>> comments = new LinkedList<Token<JavaTokenId>>();

        void add(Token<JavaTokenId> comment) {
            comments.add(comment);
        }

        boolean isEmpty() {
            return comments.isEmpty();
        }

        public Iterator<Token<JavaTokenId>> iterator() {
            return comments.iterator();
        }

        void setBounds(int[] bounds) {
            this.bounds[0] = bounds[0];
            this.bounds[1] = bounds[1];
        }

        public int[] getBounds() {
            return bounds.clone();
        }

        public void merge(CommentsCollection cc) {
            comments.addAll(cc.comments);
            this.bounds[0] = Math.min(this.bounds[0], cc.bounds[0]);
            this.bounds[1] = Math.max(this.bounds[1], cc.bounds[1]);
        }
    }

}
