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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.formatting.api.embedding;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

public final class JoinedTokenSequence<T1 extends TokenId> {

    List<TokenSequenceWrapper<T1>> tss;
    private int currentTokenSequence = -1;

    private JoinedTokenSequence(List<TokenSequenceWrapper<T1>> tss) {
        this.tss = tss;
    }

    public static <T1 extends TokenId> JoinedTokenSequence<T1> createFromTokenSequenceWrappers(List<TokenSequenceWrapper<T1>> tss) {
        return new JoinedTokenSequence<T1>(tss);
    }

    public static <T1 extends TokenId> JoinedTokenSequence<T1> createFromCodeBlocks(List<JoinedTokenSequence.CodeBlock<T1>> codeBlocks) {
        List<TokenSequenceWrapper<T1>> tss = new ArrayList<TokenSequenceWrapper<T1>>();
        for (JoinedTokenSequence.CodeBlock<T1> block : codeBlocks) {
            tss.addAll(block.tss);
        }
        return new JoinedTokenSequence<T1>(tss);
    }

    private void checkCurrentTokenSequence() {
        if (currentTokenSequence == -1) {
            throw new IllegalStateException("token position was no initialized. call moveStart or something");
        }
    }

    public Token<T1> token() {
        checkCurrentTokenSequence();
        return currentTokenSequence().token();
    }

    public TokenSequence<?> embedded() {
        checkCurrentTokenSequence();
        return currentTokenSequence().embedded();
    }

    public int index() {
        checkCurrentTokenSequence();
        int index = (currentTokenSequence+1) *1000000;
        return index + currentTokenSequence().index();
    }

    public void moveIndex(int ind) {
        checkCurrentTokenSequence();
        String s = ""+ind;
        assert s.length() > 6 : s;
        s = s.substring(s.length()-6);
        int tokenIndex = Integer.parseInt(s);
        int tokenSequence = ((ind - tokenIndex) / 1000000)-1;
        if (tokenSequence < 0 || tokenSequence >= tss.size()) {
            throw new IllegalStateException("index "+ind+" is out of boundaries "+tss );
        }
        currentTokenSequence = tokenSequence;
        currentTokenSequence().moveIndex(tokenIndex);
    }

    public TokenSequence<T1> currentTokenSequence() {
        checkCurrentTokenSequence();
        return tss.get(currentTokenSequence).getTokenSequence();
    }

    public boolean isCurrentTokenSequenceVirtual() {
        checkCurrentTokenSequence();
        return tss.get(currentTokenSequence).isVirtual();
    }

    public List<TokenSequenceWrapper<T1>> getContextDataTokenSequences() {
        return tss;
    }

    public void moveStart() {
        currentTokenSequence = 0;
        currentTokenSequence().moveStart();
    }

    public void moveEnd() {
        currentTokenSequence = tss.size()-1;
        currentTokenSequence().moveEnd();
    }

    public boolean moveNext() {
        checkCurrentTokenSequence();
        boolean moreTokens = currentTokenSequence().moveNext();

        if (!moreTokens) {
            if (currentTokenSequence < tss.size()-1) {
                currentTokenSequence++;
                currentTokenSequence().moveStart();
                moveNext();
            } else {
                return false;
            }
        }

        return true;
    }

    public boolean movePrevious() {
        checkCurrentTokenSequence();
        boolean moreTokens = currentTokenSequence().movePrevious();

        if (!moreTokens) {
            if (currentTokenSequence > 0) {
                currentTokenSequence--;
                currentTokenSequence().moveEnd();
                movePrevious();
            } else {
                return false;
            }
        }

        return true;
    }

    public int move(int offset) {
        for (int i = 0; i < tss.size(); i++) {
            TokenSequenceWrapper<T1> cdts = tss.get(i);
            if (cdts.isVirtual()) {
                continue;
            }
            TokenSequence<T1> ts = cdts.getTokenSequence();
            ts.moveStart();
            ts.moveNext();
            int start = ts.offset();
            ts.moveEnd();
            ts.movePrevious();
            int end = ts.offset() + ts.token().length();
            if (offset >= start && offset <= end) {
                currentTokenSequence = i;
                return currentTokenSequence().move(offset);
            }
        }

        return Integer.MIN_VALUE;
    }

    public boolean move(int offset, boolean forward) {
        int previous = -1;
        for (int i = 0; i < tss.size(); i++) {
            TokenSequenceWrapper<T1> cdts = tss.get(i);
            if (cdts.isVirtual()) {
                continue;
            }
            TokenSequence<T1> ts = cdts.getTokenSequence();
            ts.moveStart();
            ts.moveNext();
            int start = ts.offset();
            ts.moveEnd();
            ts.movePrevious();
            int end = ts.offset() + ts.token().length();
            if (offset >= start && offset <= end) {
                currentTokenSequence = i;
                currentTokenSequence().move(offset);
                return true;
            }
            if (forward && start > offset) {
                currentTokenSequence = i;
                currentTokenSequence().moveStart();
                return true;
            }
            if (!forward) {
                if (start > offset) {
                    if (previous != -1) {
                        currentTokenSequence = previous;
                        currentTokenSequence().moveEnd();
                        return true;
                    } else {
                        return false;
                    }
                } else if (i == tss.size()-1 && end < offset) {
                    currentTokenSequence = i;
                    currentTokenSequence().moveEnd();
                    return true;
                }
            }
            previous = i;
        }

        return false;
    }

    public int offset() {
        checkCurrentTokenSequence();
        if (isCurrentTokenSequenceVirtual()) {
            assert currentTokenSequence > 0;
            TokenSequenceWrapper<T1> cdts = tss.get(currentTokenSequence-1);
            TokenSequence<T1> ts = cdts.getTokenSequence();
            ts.moveEnd();
            ts.movePrevious();
            return ts.offset() + ts.token().length();
        } else {
            return currentTokenSequence().offset();
        }
    }

    public Language<T1> language() {
        checkCurrentTokenSequence();
        return currentTokenSequence().language();
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (TokenSequenceWrapper<T1> cdts : tss) {
            String s = "";
            if (currentTokenSequence != -1 && cdts == tss.get(currentTokenSequence)) {
                s = "CURRENT,";
            }
            sb.append("ContextDataTokenSequence["+s+"ts="+cdts.getTokenSequence().toString()+",virtual="+cdts.isVirtual()+"],");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length()-1);
        }
        return "JoinedTokenSequence["+sb.toString()+"]";
    }

    public static final class TokenSequenceWrapper<T1 extends TokenId> {
        private TokenSequence<T1> ts;
        private boolean virtual;

        public TokenSequenceWrapper(TokenSequence<T1> ts, boolean virtual) {
            this.ts = ts;
            this.virtual = virtual;
        }

        public TokenSequence<T1> getTokenSequence() {
            return ts;
        }

        public boolean isVirtual() {
            return virtual;
        }

        @Override
        public String toString() {
            return "ContextDataTokenSequence[ts="+ts+",virtual="+virtual+"]";
        }

    }


    public static final class CodeBlock<T1 extends TokenId> {
        public List<TokenSequenceWrapper<T1>> tss;

        public CodeBlock(List<TokenSequenceWrapper<T1>> tss) {
            this.tss = tss;
        }

        @Override
        public String toString() {
            return "CodeBlock[tss="+tss+"]";
        }
    }


}