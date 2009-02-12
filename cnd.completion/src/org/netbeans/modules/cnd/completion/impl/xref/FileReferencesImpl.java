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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CndAbstractTokenProcessor;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;

/**
 *
 * @author Sergey Grinev
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmFileReferences.class)
public class FileReferencesImpl extends CsmFileReferences  {

    public FileReferencesImpl() {
        /*System.err.println("FileReferencesImpl registered");
        CsmModelAccessor.getModel().addProgressListener(new CsmProgressAdapter() {

            @Override
            public void fileParsingStarted(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }

            public @Override void fileInvalidated(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }
        });*/
    }

//    private final Map<CsmFile, List<CsmReference>> cache = new HashMap<CsmFile, List<CsmReference>>();

    public void accept(CsmScope csmScope, Visitor visitor) {
        accept(csmScope, visitor, CsmReferenceKind.ALL);
    }

    public void accept(CsmScope csmScope, Visitor visitor, Set<CsmReferenceKind> kinds) {
        FileReferencesContext fileReferencesContext = new FileReferencesContext(csmScope);
        try {
            _accept(csmScope, visitor, kinds, fileReferencesContext);
        } finally {
            fileReferencesContext.clean();
        }
    }

    private void _accept(CsmScope csmScope, Visitor visitor, Set<CsmReferenceKind> kinds, FileReferencesContext fileReferncesContext) {
        if (!CsmKindUtilities.isOffsetable(csmScope) && !CsmKindUtilities.isFile(csmScope)){
            return;
        }
        CsmFile csmFile = null;

        int start, end;

        if (CsmKindUtilities.isFile(csmScope)){
            csmFile = (CsmFile) csmScope;
        } else {
            csmFile = ((CsmOffsetable)csmScope).getContainingFile();
        }

        BaseDocument doc = ReferencesSupport.getDocument(csmFile);
        if (doc == null || !csmFile.isValid()) {
            // This rarely can happen:
            // 1. if file was put on reparse and scope we have here is already obsolete
            // TODO: find new scope if API would allow that one day
            // 2. renamed
            // TODO: search by unique name
            // 3. deleted
            return;
        }
        if (CsmKindUtilities.isFile(csmScope)) {
            start = 0;
            end = Math.max(0, doc.getLength() - 1);
        } else {
            start = ((CsmOffsetable)csmScope).getStartOffset();
            end = ((CsmOffsetable)csmScope).getEndOffset();
        }

        List<CsmReferenceContext> refs = getIdentifierReferences(csmFile, doc, start,end, kinds, fileReferncesContext);

        for (CsmReferenceContext context : refs) {
            // skip 'this' if possible
            if (!isThis(context.getReference())) {
                visitor.visit(context);
            }
        }
    }

    @Override
    protected boolean isThis(CsmReference ref) {
        TokenItem<CppTokenId> refToken = ReferencesSupport.getRefTokenIfPossible(ref);
        if (refToken != null) {
            return refToken.id() == CppTokenId.THIS;
        } else {
            return super.isThis(ref);
        }
    }

    @Override
    public void visit(Collection<CsmReference> refs, ReferenceVisitor visitor) {
        FileReferencesContext fileReferencesContext = null;
        try {
            for(CsmReference ref : refs) {
                if (fileReferencesContext == null){
                    fileReferencesContext = new FileReferencesContext(ref.getContainingFile());
                }
                if (ref instanceof ReferenceImpl) {
                    ((ReferenceImpl)ref).setFileReferencesContext(fileReferencesContext);
                }
                visitor.visit(ref);
            }
        } finally {
            if (fileReferencesContext != null) {
                fileReferencesContext.clean();
            }
        }
    }

    private List<CsmReferenceContext> getIdentifierReferences(CsmFile csmFile, final BaseDocument doc,
                    final int start, final int end,
                    Set<CsmReferenceKind> kinds, FileReferencesContext fileReferncesContext) {
        boolean needAfterDereferenceUsages = kinds.contains(CsmReferenceKind.AFTER_DEREFERENCE_USAGE);
        boolean skipPreprocDirectives = !kinds.contains(CsmReferenceKind.IN_PREPROCESSOR_DIRECTIVE);
        Collection<CsmOffsetable> deadBlocks;
        if (!kinds.contains(CsmReferenceKind.IN_DEAD_BLOCK)) {
            deadBlocks = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(csmFile);
        } else {
            deadBlocks = Collections.<CsmOffsetable>emptyList();
        }
        final ReferencesProcessor tp = new ReferencesProcessor(csmFile, doc, skipPreprocDirectives, needAfterDereferenceUsages, deadBlocks, fileReferncesContext);
        doc.readLock();
        try {
            CndTokenUtilities.processTokens(tp, doc, start, end);
        } finally {
            doc.readUnlock();
        }
        return tp.references;
    }

    private static final class ReferencesProcessor extends CndAbstractTokenProcessor<Token<CppTokenId>> {
        /*package*/ final List<CsmReferenceContext> references = new ArrayList<CsmReferenceContext>();
        private final Collection<CsmOffsetable> deadBlocks;
        private final boolean needAfterDereferenceUsages;
        private final boolean skipPreprocDirectives;
        private final CsmFile csmFile;
        private final BaseDocument doc;
        private final ReferenceContextBuilder contextBuilder;
        private final FileReferencesContext fileReferncesContext;
        private CppTokenId derefToken;
        private BlockConsumer blockConsumer;
        private boolean afterParen = false;
        
        ReferencesProcessor(CsmFile csmFile, BaseDocument doc,
             boolean skipPreprocDirectives, boolean needAfterDereferenceUsages,
             Collection<CsmOffsetable> deadBlocks, FileReferencesContext fileReferncesContext) {
            this.deadBlocks = deadBlocks;
            this.needAfterDereferenceUsages = needAfterDereferenceUsages;
            this.skipPreprocDirectives = skipPreprocDirectives;
            this.csmFile = csmFile;
            this.doc = doc;
            this.contextBuilder = new ReferenceContextBuilder();
            this.fileReferncesContext = fileReferncesContext;
        }

        @Override
        public boolean token(Token<CppTokenId> token, int tokenOffset) {
            if (blockConsumer != null) {
                if (blockConsumer.isLastToken(token)) {
                    blockConsumer = null;
                }
                return false;
            }
            boolean skip = false;
            boolean needEmbedding = false;
            switch (token.id()) {
                case PREPROCESSOR_DIRECTIVE:
                    needEmbedding = !skipPreprocDirectives;
                    break;
                case IDENTIFIER:
                case PREPROCESSOR_IDENTIFIER:
                case THIS:
                {
                    skip = !needAfterDereferenceUsages && derefToken != null;
                    if (!skip && !deadBlocks.isEmpty()) {
                        skip = isInDeadBlock(tokenOffset, deadBlocks);
                    }
                    ReferenceImpl ref = ReferencesSupport.createReferenceImpl(
                            csmFile, doc, tokenOffset, CndTokenUtilities.createTokenItem(token, tokenOffset), derefToken == null?
                                null : CsmReferenceKind.AFTER_DEREFERENCE_USAGE);
                    contextBuilder.reference(ref, derefToken);
                    ref.setFileReferencesContext(fileReferncesContext);
                    derefToken = null;
                    if (!skip) {
                        references.add(contextBuilder.getContext());
                    }
                    break;
                }
                case DOT:
                case DOTMBR:
                case ARROW:
                case ARROWMBR:
                case SCOPE:
                    derefToken = token.id();
                    break;
                case LBRACE:
                    if(afterParen) {
                        // Compiler extension "({...})"
                        blockConsumer = new BlockConsumer(CppTokenId.LBRACE, CppTokenId.RBRACE);
                    } else {
                        contextBuilder.open(token.id());
                    }
                    derefToken = null;
                    break;
                case LBRACKET:
                case LPAREN:
                case LT:
                    contextBuilder.open(token.id());
                    derefToken = null;
                    break;
                case RBRACE:
                case RBRACKET:
                case RPAREN:
                case GT:
                    contextBuilder.close(token.id());
                    derefToken = null;
                    break;
                case __ATTRIBUTE__:
                case __ATTRIBUTE:
                case _DECLSPEC:
                case __DECLSPEC:
                case ASM:
                case __ASM:
                case __ASM__:
                    blockConsumer = new BlockConsumer(CppTokenId.LPAREN, CppTokenId.RPAREN);
                    derefToken = null;
                    break;
                case _ASM:
                    blockConsumer = new BlockConsumer(CppTokenId.LBRACE, CppTokenId.RBRACE);
                    derefToken = null;
                    break;
                case WHITESPACE:
                case NEW_LINE:
                case BLOCK_COMMENT:
                case LINE_COMMENT:
                case TEMPLATE:
                    // OK, do nothing
                    break;
                default:
                    contextBuilder.other(token.id());
                    derefToken = null;
            }

            // Initializing afterParen flag
            // This flag is used for detection of compiler extensions "({...})"
            switch (token.id()) {
                case LPAREN:
                    afterParen = true;
                    break;
                case WHITESPACE:
                case NEW_LINE:
                case BLOCK_COMMENT:
                case LINE_COMMENT:
                    break;
                default:
                    afterParen = false;
            }
            
            return needEmbedding;
        }
    }

    private static final class ReferenceContextBuilder {

        private static final int FULLCOPY_INTERVAL = 50;
        private ReferenceContextImpl context;
        private final List<CppTokenId> brackets;
        private final List<Integer> pushes;
        private int snapshots;

        public ReferenceContextBuilder() {
            context = new ReferenceContextImpl();
            brackets = new ArrayList<CppTokenId>();
            pushes = new ArrayList<Integer>();
            pushes.add(0);
        }

        public void open(CppTokenId leftBracket) {
            if (peek(pushes) == 0 && peek(brackets) != null) {
                // insert a dummy reference if needed
                context.push(peek(brackets), null);
                pop(pushes);
                pushes.add(1);
            }
            brackets.add(leftBracket);
            pushes.add(0);
        }

        public void close(CppTokenId rightBracket) {
            if (match(peek(brackets), rightBracket)) {
                // close corresponding bracket if possible
                pop(brackets);
                for (int i = 0; i < peek(pushes); ++i) {
                    context.pop();
                }
                pop(pushes);
            }
        }

        public void other(CppTokenId token) {
            if (token == CppTokenId.SEMICOLON && peek(brackets) == CppTokenId.LT) {
                // semicolon can't appear inside angle brackets
                close(CppTokenId.GT);
            }
            for (int i = 0; i < peek(pushes); ++i) {
                context.pop();
            }
            pop(pushes);
            pushes.add(0);
        }

        public void reference(CsmReference ref, CppTokenId derefToken) {
            int pushCount = 0;
            if (derefToken == null) {
                other(CppTokenId.IDENTIFIER);
                context.push(peek(brackets), ref);
                ++pushCount;
            } else {
                if (peek(pushes) == 0 && peek(brackets) != null) {
                    context.push(peek(brackets), null);
                    ++pushCount;
                }
                context.push(derefToken, ref);
                ++pushCount;
            }
            pushes.add(pop(pushes) + pushCount);
        }

        private static<T> T peek(List<T> list) {
            if (list.isEmpty()) {
                return null;
            } else {
                return list.get(list.size() - 1);
            }
        }

        private static<T> T pop(List<T> list) {
            if (list == null || list.isEmpty()) {
                return null;
            } else {
                return list.remove(list.size() - 1);
            }
        }

        private static boolean match(CppTokenId l, CppTokenId r) {
            return l == CppTokenId.LBRACE && r == CppTokenId.RBRACE
                    || l == CppTokenId.LBRACKET && r == CppTokenId.RBRACKET
                    || l == CppTokenId.LPAREN && r == CppTokenId.RPAREN
                    || l == CppTokenId.LT && r == CppTokenId.GT;
        }

        public CsmReferenceContext getContext() {
            CsmReferenceContext snapshot;
            if (FULLCOPY_INTERVAL <= snapshots++) {
                snapshot = new ReferenceContextImpl(context, true);
                snapshots = 0;
            } else {
                snapshot = context;
            }
            context = new ReferenceContextImpl(snapshot, false);
            return snapshot;
        }

        @Override
        public String toString() {
            return String.valueOf(context);
        }

    }

    private static boolean isInDeadBlock(int startOffset, Collection<CsmOffsetable> deadBlocks) {
        for (CsmOffsetable csmOffsetable : deadBlocks) {
            if (csmOffsetable.getStartOffset() > startOffset) {
                return false;
            }
            if (csmOffsetable.getEndOffset() > startOffset) {
                return true;
            }
        }
        return false;
    }
    
    private static class BlockConsumer {
        private final CppTokenId openBracket;
        private final CppTokenId closeBracket;
        private int depth;
        public BlockConsumer(CppTokenId openBracket, CppTokenId closeBracket) {
            this.openBracket = openBracket;
            this.closeBracket = closeBracket;
            depth = 0;
        }
        
        public boolean isLastToken(Token<CppTokenId> token) {
            boolean stop = false;
            if (token.id() == openBracket) {
                ++depth;
            } else if (token.id() == closeBracket) {
                --depth;
                stop = depth <= 0;
            }
            return stop;
        }
    }
}
