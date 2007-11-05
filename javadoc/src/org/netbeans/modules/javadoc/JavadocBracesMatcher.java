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
package org.netbeans.modules.javadoc;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 *
 * @author Vita Stejskal
 */
public final class JavadocBracesMatcher implements BracesMatcher, BracesMatcherFactory {

    private static final Logger LOG = Logger.getLogger(JavadocBracesMatcher.class.getName());
    
    private final MatcherContext context;
    
    private TokenSequence<? extends TokenId> jdocSeq;
    private int jdocStart;
    private int jdocEnd;

//    private int [] matchingArea;
    
    private BracesMatcher defaultMatcher;
    
    public JavadocBracesMatcher() {
        this(null);
    }

    private JavadocBracesMatcher(MatcherContext context) {
        this.context = context;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int[] findOrigin() throws BadLocationException, InterruptedException {
        int caretOffset = context.getSearchOffset();
        boolean backward = context.isSearchingBackward();
        
        TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
        List<TokenSequence<?>> sequences = th.embeddedTokenSequences(caretOffset, backward);

        for(int i = sequences.size() - 1; i >= 0; i--) {
            TokenSequence<? extends TokenId> seq = sequences.get(i);
            if (seq.language() == JavadocTokenId.language()) {
                jdocSeq = seq;
                if (i > 0) {
                    TokenSequence<? extends TokenId> javaSeq = sequences.get(i - 1);
                    jdocStart = javaSeq.offset();
                    jdocEnd = javaSeq.offset() + javaSeq.token().length();
                } else {
                    // jdocSeq is the top level sequence, ie the whole document is just javadoc
                    jdocStart = 0;
                    jdocEnd = context.getDocument().getLength();
                }
                break;
            }
        }

        if (jdocSeq == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Not javadoc TokenSequence."); //NOI18N
            }
            return null;
        }
        
//        if (caretOffset >= jdocStart && 
//            ((backward && caretOffset <= jdocStart + 3) ||
//            (!backward && caretOffset < jdocStart + 3))
//        ) {
//            matchingArea = new int [] { jdocEnd - 2, jdocEnd };
//            return new int [] { jdocStart, jdocStart + 3 };
//        }
//
//        if (caretOffset <= jdocEnd && 
//            ((backward && caretOffset > jdocEnd - 2) ||
//            (!backward && caretOffset >= jdocEnd - 2))
//        ) {
//            matchingArea = new int [] { jdocStart, jdocStart + 3 };
//            return new int [] { jdocEnd - 2, jdocEnd };
//        }
        
        // look for tags first
        jdocSeq.move(caretOffset);
        if (jdocSeq.moveNext()) {
            if (isTag(jdocSeq.token())) {
                if (jdocSeq.offset() < caretOffset || !backward) {
                    return prepareOffsets(jdocSeq, true);
                }
            }

            while(moveTheSequence(jdocSeq, backward, context.getLimitOffset())) {
                if (isTag(jdocSeq.token())) {
                    return prepareOffsets(jdocSeq, true);
                }
            }
        }

        defaultMatcher = BracesMatcherSupport.defaultMatcher(context, jdocStart, jdocEnd);
        return defaultMatcher.findOrigin();
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        if (defaultMatcher != null) {
            return defaultMatcher.findMatches();
        }
    
//        if (matchingArea != null) {
//            return matchingArea;
//        }
        
        assert jdocSeq != null : "No javadoc token sequence"; //NOI18N
        
        Token<? extends TokenId> tag = jdocSeq.token();
        assert tag.id() == JavadocTokenId.HTML_TAG : "Wrong token"; //NOI18N
        
        if (isSingleTag(tag)) {
            return new int [] { jdocSeq.offset(), jdocSeq.offset() + jdocSeq.token().length() };
        }
        
        boolean backward = !isOpeningTag(tag);
        int cnt = 0;
        
        while(moveTheSequence(jdocSeq, backward, -1)) {
            if (!isTag(jdocSeq.token())) {
                continue;
            }
            
            if (matchTags(tag, jdocSeq.token())) {
                if ((backward && !isOpeningTag(jdocSeq.token())) ||
                    (!backward && isOpeningTag(jdocSeq.token()))
                ) {
                    cnt++;
                } else {
                    if (cnt == 0) {
                        return prepareOffsets(jdocSeq, false);
                    } else {
                        cnt--;
                    }
                }
            }
        }
        
        return null;
    }

    // -----------------------------------------------------
    // private implementation
    // -----------------------------------------------------

    private boolean moveTheSequence(TokenSequence<? extends TokenId> seq, boolean backward, int offsetLimit) {
        if (backward) {
            if (seq.movePrevious()) {
                int e = seq.offset() + seq.token().length();
                return offsetLimit == -1 ? true : e > offsetLimit;
            }
        } else {
            if (seq.moveNext()) {
                int s = seq.offset();
                return offsetLimit == -1 ? true : s < offsetLimit;
            }
        }
        return false;
    }

    private static boolean isTag(Token<? extends TokenId> tag) {
        CharSequence s = tag.text();
        int l = s.length();
        
        boolean b = tag.id() == JavadocTokenId.HTML_TAG &&
            l >= 3 &&
            s.charAt(0) == '<' && //NOI18N
            s.charAt(l - 1) == '>'; //NOI18N
        
        if (b) {
            if (s.charAt(1) == '/') { //NOI18N
                b = l >= 4 && Character.isLetterOrDigit(s.charAt(2));
            } else {
                b = Character.isLetterOrDigit(s.charAt(1));
            }
        }
        
        return b;
    }
    
    private static boolean isSingleTag(Token<? extends TokenId> tag) {
        return TokenUtilities.endsWith(tag.text(), "/>"); //NOI18N
    }
    
    private static boolean isOpeningTag(Token<? extends TokenId> tag) {
        return !TokenUtilities.startsWith(tag.text(), "</"); //NOI18N
    }
    
    private static boolean matchTags(Token<? extends TokenId> t1, Token<? extends TokenId> t2) {
        assert t1.length() >= 2 && t1.text().charAt(0) == '<' : t1 + " is not a tag."; //NOI18N
        assert t2.length() >= 2 && t2.text().charAt(0) == '<' : t2 + " is not a tag."; //NOI18N
        
        int idx1 = 1;
        int idx2 = 1;
        
        if (t1.text().charAt(1) == '/') {
            idx1++;
        } 
        
        if (t2.text().charAt(1) == '/') {
            idx2++;
        }
        
        for( ; idx1 < t1.length() && idx2 < t2.length(); idx1++, idx2++) {
            char ch1 = t1.text().charAt(idx1);
            char ch2 = t2.text().charAt(idx2);
            
            if (ch1 != ch2) {
                return !Character.isLetterOrDigit(ch1) || !Character.isLetterOrDigit(ch2);
            }
            
            if (!Character.isLetterOrDigit(ch1)) {
                return true;
            }
        }
        
        return false;
    }

    private static int [] prepareOffsets(TokenSequence<? extends TokenId> seq, boolean includeToken) {
        int s = seq.offset();
        int e = seq.offset() + seq.token().length();
        CharSequence token = seq.token().text();
        
        if (token.charAt(1) == '/') { //NOI18N
            return new int [] { s, e };
        } else {
            int he = e;
            
            for(int i = 1; i < token.length(); i++) {
                char ch = token.charAt(i);
                if (!Character.isLetterOrDigit(ch) && ch != '>') { //NOI18N
                    he = s + i;
                    break;
                }
            }
            
            if (includeToken) {
                // first the boundaries, than the highlight
                return new int [] { s, e, s, he };
            } else {
                return new int [] { s, he };
            }
        }
    }
    
    // -----------------------------------------------------
    // BracesMatcherFactory implementation
    // -----------------------------------------------------
    
    /** */
    public BracesMatcher createMatcher(MatcherContext context) {
        return new JavadocBracesMatcher(context);
    }

}
