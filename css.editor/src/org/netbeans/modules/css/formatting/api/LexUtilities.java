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

package org.netbeans.modules.css.formatting.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence.TokenSequenceWrapper;
import org.netbeans.modules.gsf.api.EmbeddingModel;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 */
public class LexUtilities {

    private LexUtilities(){};

    /** Find given language token sequence (in case it's embedded in something else at the top level */
    public static <T extends TokenId> TokenSequence<T> getTokenSequence(BaseDocument doc, int offset, Language<T> language) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        return getTokenSequence(th, offset, language);
    }

    /** Find given language token sequence (in case it's embedded in something else at the top level */
    public static <T extends TokenId>  TokenSequence<T> getTokenSequence(TokenHierarchy<Document> th, int offset, Language<T> language) {
        TokenSequence<T> ts = th.tokenSequence(language);

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == language) {
                    ts = t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == language) {
                        ts = t;

                        break;
                    }
                }
            }
        }

        return ts;
    }

    public static Language<? extends TokenId> getLanguage(BaseDocument doc, int offset) {
        TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
        return getLanguage(th, offset);
    }

    public static Language<? extends TokenId> getLanguage(TokenHierarchy<BaseDocument> th, int offset) {
        List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

        if (list.size() == 0) {
            return null;
        }
        return list.get(list.size()-1).language();
    }

    public static <T extends TokenId> TokenSequence<T> getPositionedSequence(BaseDocument doc, int offset, Language<T> language) {
        return getPositionedSequence(doc, offset, true, language);
    }

    public static <T extends TokenId>  TokenSequence<T> getPositionedSequence(BaseDocument doc, int offset, boolean lookBack, Language<T> language) {
        TokenSequence<T> ts = getTokenSequence(doc, offset, language);

        if (ts != null) {
            try {
                ts.move(offset);
            } catch (AssertionError e) {
                DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

                if (dobj != null) {
                    Exceptions.attachMessage(e, FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
                }

                throw e;
            }

            if (!lookBack && !ts.moveNext()) {
                return null;
            } else if (lookBack && !ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            return ts;
        }

        return null;
    }

    public static <T extends TokenId> Token<T> findNext(TokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }

    public static <T extends TokenId> Token<T> findNext(JoinedTokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }

    public static <T extends TokenId> Token<T> findPrevious(TokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }

    public static <T extends TokenId> Token<T> findPrevious(JoinedTokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }

    private static <T extends TokenId> TokenSequence<T> getVirtualTokens(
            Collection<? extends TranslatedSource> translatedSources,
            int startOffset, int endOffset, Language<T> language) {
        for (TranslatedSource ts : translatedSources) {
            TokenSequence<T> ts2 = getVirtualTokens(ts, startOffset, endOffset, language);
            if (ts2 != null) {
                return ts2;
            }
        }
        return null;
    }

    private static <T extends TokenId> TokenSequence<T> getVirtualTokens(
            TranslatedSource translatedSource,
            int startOffset, int endOffset, Language<T> language) {
        String source = translatedSource.getSource();
        int start = translatedSource.getAstOffset(startOffset);
        int end = translatedSource.getAstOffset(endOffset);
        if (start == -1 || end == -1) {
            return null;
        }
        source = source.substring(start, end);
        if (source.length() == 0) {
            return null;
        }
        return TokenHierarchy.create(source, language).tokenSequence(language);
    }

    private static EmbeddingModel getEmbedding(String targetMimeType, String sourceMimeType) {
        Collection<? extends EmbeddingModel> models = getEmbeddingModels();

        for (EmbeddingModel model : models) {
            if (model.getTargetMimeType().equals(targetMimeType) &&
                model.getSourceMimeTypes().contains(sourceMimeType)) {
                return model;
            }
        }

        return null;
    }

    private static Collection<? extends EmbeddingModel> getEmbeddingModels() {
        if (embeddingModels == null) {
            embeddingModels = Lookup.getDefault().lookupAll(EmbeddingModel.class);
        }

        return embeddingModels;
    }

    private static Collection<? extends EmbeddingModel> embeddingModels;

    public static int getTokenSequenceEndOffset(TokenSequence<? extends TokenId> ts) {
        int currentIndex = ts.index();
        ts.moveEnd();
        ts.movePrevious();
        int offset = ts.offset() + ts.token().length();
        ts.move(currentIndex);
        return offset;
    }

    public static int getTokenSequenceEndOffset(JoinedTokenSequence<? extends TokenId> ts) {
        int currentIndex = ts.index();
        ts.moveEnd();
        ts.movePrevious();
        int offset = ts.offset() + ts.token().length();
        ts.move(currentIndex);
        return offset;
    }

    public static int getTokenSequenceStartOffset(TokenSequence<? extends TokenId> ts) {
        int currentIndex = ts.index();
        ts.moveStart();
        ts.moveNext();
        int offset = ts.offset();
        ts.move(currentIndex);
        return offset;
    }

    public static int getTokenSequenceStartOffset(JoinedTokenSequence<? extends TokenId> ts) {
        int currentIndex = ts.index();
        ts.moveStart();
        ts.moveNext();
        int offset = ts.offset();
        ts.move(currentIndex);
        return offset;
    }

    public static <T1 extends TokenId> Token<T1> getTokenAtOffset(JoinedTokenSequence<T1> ts, int offset) {
        int currentIndex = ts.index();
        ts.move(offset);
        ts.moveNext();
        Token<T1> t = ts.token();
        ts.moveIndex(currentIndex);
        return t;
    }

    public static <T1 extends TokenId> List<TokenSequence<T1>> getEmbeddedTokenSequences(BaseDocument doc, Language<T1> language, int start, int end) {
        TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
        List<LanguagePath> lps = new ArrayList<LanguagePath>();
        for (LanguagePath lp : th.languagePaths()) {
            if (lp.endsWith(LanguagePath.get(language))) {
                lps.add(lp);
            }
        }
        List<TokenSequence<T1>> tss = new ArrayList<TokenSequence<T1>>();
        for (LanguagePath lp : lps) {
            List<TokenSequence<?>> tss2 = th.tokenSequenceList(lp, start, end);
            for (TokenSequence<?> ts2 : tss2) {
                ts2.moveStart();
                ts2.moveNext();
                tss.add((TokenSequence<T1>)ts2);
            }
        }
        Collections.sort(tss, new Comparator<TokenSequence<T1>>() {
            public int compare(TokenSequence<T1> o1, TokenSequence<T1> o2) {
                assert o1.offset() != o2.offset(); // should never have two equal TokenSequence
                return o1.offset() - o2.offset();
            }
        });
        return tss;
    }

    private static <T1 extends TokenId> List<JoinedTokenSequence.CodeBlock<T1>> calculateCodeBlock(List<TokenSequence<T1>> tss,
            Collection<? extends TranslatedSource> translatedSources
        ) throws BadLocationException {

        List<JoinedTokenSequence.CodeBlock<T1>> blocks = new ArrayList<JoinedTokenSequence.CodeBlock<T1>>();

        for (int i=0; i<tss.size(); i++) {
            TokenSequence<T1> ts =tss.get(i);

            List<TokenSequenceWrapper<T1>> tss2 = new ArrayList<TokenSequenceWrapper<T1>>();
            tss2.add(new TokenSequenceWrapper(ts, false));

            // try to find additional token sequences which comprise this language block:
            for (int j=i+1; j<tss.size(); j++) {
                TokenSequence<T1> prev = tss.get(j-1);
                prev.moveEnd();
                prev.movePrevious();
                TokenSequence<T1> next = tss.get(j);
                next.moveStart();
                next.moveNext();
                // check whether current token sequence is continuation of previous one:
                TokenSequence<T1> tsVirtual = LexUtilities.getVirtualTokens(translatedSources, prev.offset()+prev.token().length(), next.offset(), ts.language());
                if (tsVirtual != null) {
                    tss2.add(new TokenSequenceWrapper(tsVirtual, true));
                    tss2.add(new TokenSequenceWrapper(next, false));
                    i++;
                } else {
                    break;
                }
            }


            blocks.add(new JoinedTokenSequence.CodeBlock<T1>(tss2));
        }

        return blocks;
    }

    public static <T1 extends TokenId> List<JoinedTokenSequence.CodeBlock<T1>> createCodeBlocks(
            BaseDocument doc, Language<T1> language) throws BadLocationException {
        String mimeType = (String)doc.getProperty("mimeType"); // NOI18N
        boolean isEmbedded = !language.mimeType().equals(mimeType);
        return createCodeBlocks(doc, language, isEmbedded);
    }

    public static <T1 extends TokenId> List<JoinedTokenSequence.CodeBlock<T1>> createCodeBlocks(
            BaseDocument doc, Language<T1> language, boolean isEmbedded) throws BadLocationException {
        Collection<? extends TranslatedSource> translatedSources = null;
        List<TokenSequence<T1>> tss = LexUtilities.getEmbeddedTokenSequences(doc, language, 0, doc.getLength());
        if (tss.size() == 0) {
            return null;
        }
        if (isEmbedded) {
            EmbeddingModel model = LexUtilities.getEmbedding(language.mimeType(), (String)doc.getProperty("mimeType"));
            if (model != null) {
                translatedSources = model.translate(doc);
            }
//        } else {
//            tss = new ArrayList<TokenSequence<T1>>();
//            tss.add(TokenHierarchy.get(doc).tokenSequence(language));
        }
        return LexUtilities.calculateCodeBlock(tss, translatedSources);
    }

}
