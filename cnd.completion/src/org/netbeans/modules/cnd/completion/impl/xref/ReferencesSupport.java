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

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver.Scope;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery.QueryScope;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Parameters;
import org.openide.util.UserQuestionException;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.editor.AtomicLockDocument;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ReferencesSupport {

    private ReferencesSupport() {
    }

    /**
     * converts (line, col) into offset. Line and column info are 1-based, so
     * the start of document is (1,1)
     */
    public static int getDocumentOffset(BaseDocument doc, int lineIndex, int colIndex) {
        return Utilities.getRowStartFromLineOffset(doc, lineIndex -1) + (colIndex - 1);
    }

    public static BaseDocument getBaseDocument(final String absPath) throws DataObjectNotFoundException, IOException {
        File file = new File(absPath);
        // convert file into file object
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null) {
            return null;
        }
        DataObject dataObject = DataObject.find(fileObject);
        EditorCookie  cookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
        if (cookie == null) {
            throw new IllegalStateException("Given file (\"" + dataObject.getName() + "\") does not have EditorCookie."); // NOI18N
        }

        StyledDocument doc = null;
        try {
            doc = cookie.openDocument();
        } catch (UserQuestionException ex) {
            ex.confirmed();
            doc = cookie.openDocument();
        }

        return doc instanceof BaseDocument ? (BaseDocument)doc : null;
    }

    public static CsmObject findReferencedObject(CsmFile csmFile, BaseDocument doc, int offset) {
        return findReferencedObject(csmFile, doc, offset, null);
    }

    /*static*/ static CsmObject findOwnerObject(CsmFile csmFile, BaseDocument baseDocument, int offset, Token<CppTokenId> token) {
        CsmObject csmOwner = CsmOffsetResolver.findObject(csmFile, offset);
        return csmOwner;
    }

    /*package*/ static CsmObject findReferencedObject(CsmFile csmFile, BaseDocument doc, int offset, Token<CppTokenId> jumpToken) {
        CsmObject csmItem = null;
        // emulate hyperlinks order
        // first ask includes handler if offset in include sring token
        CsmInclude incl = null;
        if (jumpToken == null) {
            try {
                doc.atomicLock();
                jumpToken = CndTokenUtilities.getOffsetTokenCheckPrev(doc, offset);
            } finally {
                doc.atomicUnlock();
            }
        }
        if (jumpToken != null) {
            switch (jumpToken.id()) {
                case PREPROCESSOR_SYS_INCLUDE:
                case PREPROCESSOR_USER_INCLUDE:
                    // look for include directive
                    incl = findInclude(csmFile, offset);
                    break;
            }
        }

        csmItem = incl == null ? null : incl.getIncludeFile();

        // if failed => ask declarations handler
        if (csmItem == null) {
            csmItem = findDeclaration(csmFile, doc, jumpToken, offset);
        }
        return csmItem;
    }

    public static CsmInclude findInclude(CsmFile csmFile, int offset) {
        assert (csmFile != null);
        return CsmOffsetUtilities.findObject(csmFile.getIncludes(), null, offset);
    }

    public static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            Token tokenUnderOffset, final int offset) {
        // fast check, if possible
        int[] idFunBlk = null;
        CsmObject csmItem = null;
        CsmObject objUnderOffset = CsmOffsetResolver.findObject(csmFile, offset);
        // TODO: it would be great to check position in named element, but we don't
        // support this information yet, so

        // fast check for enumerators
        if (CsmKindUtilities.isEnumerator(objUnderOffset)) {
            CsmEnumerator enmrtr = (CsmEnumerator)objUnderOffset;
            if (enmrtr.getExplicitValue() == null) {
                csmItem = enmrtr;
            }
        } else if (false && CsmKindUtilities.isVariableDeclaration(objUnderOffset)) {
            // turned off, due to the problems like
            // Cpu MyCpu(type, 0, amount);
            // initialization part is part of variable => we need info about name position exactly
            CsmVariable var = (CsmVariable)objUnderOffset;
            if (var.getName().length() > 0 && !var.isExtern()) {
                // not work yet for arrays declarations IZ#130678
                // not work yet for elements with init value IZ#130684
                if ((var.getInitialValue() == null) && (var.getType() != null) && (var.getType().getArrayDepth() == 0)) {
                    csmItem = var;
                }
            }
        }
        if (csmItem == null) {
            try {
                if (doc instanceof BaseDocument) {
                    idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock((BaseDocument)doc, offset);
                }
            } catch (BadLocationException ex) {
                // skip it
            }
            // check but not for function call
            if (idFunBlk != null && idFunBlk.length != 3) {
                csmItem = findDeclaration(csmFile, doc, tokenUnderOffset, offset, QueryScope.SMART_QUERY);
            }
        }
        // then full check if needed
        csmItem = csmItem != null ? csmItem : findDeclaration(csmFile, doc, tokenUnderOffset, offset, QueryScope.GLOBAL_QUERY);
        // if still null try macro info from file (IZ# 130897)
        if (csmItem == null) {
            List<CsmReference> macroUsages = CsmFileInfoQuery.getDefault().getMacroUsages(csmFile);
            for (CsmReference macroRef : macroUsages) {
                if (macroRef.getStartOffset() <= offset && offset <= macroRef.getEndOffset()) {
                    csmItem = macroRef.getReferencedObject();
                    assert csmItem != null : "must be referenced macro" + macroRef;
                }
            }
        }
        return csmItem;
    }

    public static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            Token<CppTokenId> tokenUnderOffset, final int offset, final QueryScope queryScope) {
        assert csmFile != null;
        if (tokenUnderOffset == null) {
            try {
                if (doc instanceof AtomicLockDocument) {
                    ((AtomicLockDocument)doc).atomicLock();
                }
            } finally {
                if (doc instanceof AtomicLockDocument) {
                    ((AtomicLockDocument) doc).atomicUnlock();
                }
            }
        }
        tokenUnderOffset = tokenUnderOffset != null ? tokenUnderOffset : CndTokenUtilities.getOffsetTokenCheckPrev(doc, offset);
        // no token in document under offset position
        if (tokenUnderOffset == null) {
            return null;
        }
        CsmObject csmObject = null;
        // support for overloaded operators
        if (tokenUnderOffset.id() == CppTokenId.OPERATOR) {
            CsmObject foundObject = CsmOffsetResolver.findObject(csmFile, offset);
            csmObject = foundObject;
            if (CsmKindUtilities.isFunction(csmObject)) {
                CsmFunction decl = null;
                if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                    decl = ((CsmFunctionDefinition)csmObject).getDeclaration();
                } else if (CsmKindUtilities.isFriendMethod(csmObject)) {
                    decl = ((CsmFriendFunction)csmObject).getReferencedFunction();
                }
                if (decl != null) {
                    csmObject = decl;
                }
            } else {
                csmObject = null;
            }
        }
        if (csmObject == null) {
            // try with code completion engine
            csmObject = CompletionUtilities.findItemAtCaretPos(null, doc, CsmCompletionProvider.getCompletionQuery(csmFile, queryScope), offset);
        }
        return csmObject;
    }

    /*package*/ static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, int offset) {
        try {
            doc.atomicLock();
            Token token = CndTokenUtilities.getOffsetTokenCheckPrev(doc, offset);
            ReferenceImpl ref = null;
            if (isSupportedToken(token)) {
                ref = createReferenceImpl(file, doc, offset, token, null);
            }
            return ref;
        } finally {
            doc.atomicUnlock();
        }
    }

//    /*package*/ static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, TokenItem tokenItem) {
//        Token token = new Token(tokenItem);
//        ReferenceImpl ref = createReferenceImpl(file, doc, tokenItem.getOffset(), token);
//        return ref;
//    }

    public static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, int offset, Token token, CsmReferenceKind kind) {
        assert token != null;
        assert file != null : "null file for document " + doc + " on offset " + offset + " " + token;
        ReferenceImpl ref = new ReferenceImpl(file, doc, offset, token, kind);
        return ref;
    }

    private static boolean isSupportedToken(Token<CppTokenId> token) {
        return token != null &&
                (CsmIncludeHyperlinkProvider.isSupportedToken(token) || CsmHyperlinkProvider.isSupportedToken(token));
    }

    public static Scope fastCheckScope(CsmReference ref) {
        Parameters.notNull("ref", ref); // NOI18N
        CsmObject target = getTargetIfPossible(ref);
        if (target == null) {
            // try to resolve using only local context
            int offset = getRefOffset(ref);
            BaseDocument doc = getRefDocument(ref);
            if (doc != null) {
                Token token = getRefTokenIfPossible(ref);
                target = findDeclaration(ref.getContainingFile(), doc, token, offset, QueryScope.LOCAL_QUERY);
                setResolvedInfo(ref, target);
            }
        }
        return getTargetScope(target);
    }

    private static Scope getTargetScope(CsmObject obj) {
        if (obj == null) {
            return Scope.UNKNOWN;
        }
        if (isLocalElement(obj)) {
            return Scope.LOCAL;
        } else if (isFileLocalElement(obj)) {
            return Scope.FILE_LOCAL;
        } else {
            return Scope.GLOBAL;
        }
    }

    private static CsmObject getTargetIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getTarget();
        }
        return null;
    }

    private static Token getRefTokenIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getToken();
        } else {
            return null;
        }
    }

    private static CsmReferenceKind getRefKindIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getKindImpl();
        } else {
            return null;
        }
    }

    private static BaseDocument getRefDocument(CsmReference ref) {
        if (ref instanceof DocOffsetableImpl) {
            return ((DocOffsetableImpl)ref).getDocument();
        } else {
            CsmFile file = ref.getContainingFile();
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
            Document doc = null;
            if (ces != null) {
                doc = ces.getDocument();
            }
            return doc instanceof BaseDocument ? (BaseDocument)doc : null;
        }
    }

    private static int getRefOffset(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getOffset();
        } else {
            return (ref.getStartOffset() + ref.getEndOffset() + 1) / 2;
        }
    }

    private static void setResolvedInfo(CsmReference ref, CsmObject target) {
        if (target != null && (ref instanceof ReferenceImpl)) {
            ((ReferenceImpl)ref).setTarget(target);
        }
    }

    private static boolean isLocalElement(CsmObject decl) {
        assert decl != null;
        CsmObject scopeElem = decl;
        while (CsmKindUtilities.isScopeElement(scopeElem)) {
            CsmScope scope = ((CsmScopeElement)scopeElem).getScope();
            if (CsmKindUtilities.isFunction(scope)) {
                return true;
            } else if (CsmKindUtilities.isScopeElement(scope)) {
                scopeElem = ((CsmScopeElement)scope);
            } else {
                break;
            }
        }
        return false;
    }

    private static boolean isFileLocalElement(CsmObject decl) {
        assert decl != null;
        if (CsmBaseUtilities.isDeclarationFromUnnamedNamespace(decl)) {
            return true;
        } else if (CsmKindUtilities.isFileLocalVariable(decl)) {
            return true;
        } else if (CsmKindUtilities.isFunction(decl)) {
            return CsmBaseUtilities.isFileLocalFunction(((CsmFunction)decl));
        }
        return false;
    }

    static BaseDocument getDocument(CsmFile file) {
        BaseDocument doc = null;
        try {
            doc = ReferencesSupport.getBaseDocument(file.getAbsolutePath().toString());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return doc;
    }

    static CsmReferenceKind getReferenceKind(CsmReference ref) {
        CsmReferenceKind kind = CsmReferenceKind.UNKNOWN;
        CsmObject owner = ref.getOwner();
        if (CsmKindUtilities.isType(owner) || CsmKindUtilities.isInheritance(owner)) {
            kind = getReferenceUsageKind(ref);
        } else if (CsmKindUtilities.isInclude(owner)) {
            kind = CsmReferenceKind.DIRECT_USAGE;
        } else {
            CsmObject target = ref.getReferencedObject();
            if (target == null) {
                kind = getReferenceUsageKind(ref);
            } else {
                CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(target, true);
                CsmObject targetDecl = decDef[0];
                CsmObject targetDef = decDef[1];
                assert targetDecl != null;
                kind = CsmReferenceKind.DIRECT_USAGE;
                if (owner != null) {
                    if (owner.equals(targetDecl)) {
                        kind = CsmReferenceKind.DECLARATION;
                    } else if (owner.equals(targetDef)) {
                        kind = CsmReferenceKind.DEFINITION;
                    } else {
                        kind = getReferenceUsageKind(ref);
                    }
                }
            }
        }
        return kind;
    }

    static CsmReferenceKind getReferenceUsageKind(CsmReference ref) {
        CsmReferenceKind kind = CsmReferenceKind.DIRECT_USAGE;
        if (ref instanceof ReferenceImpl) {
            CsmReferenceKind implKind = getRefKindIfPossible(ref);
            if (implKind != null) {
                return implKind;
            }
            Document doc = getRefDocument(ref);
            int offset = ref.getStartOffset();
            try {
                if (doc instanceof AtomicLockDocument) {
                    ((AtomicLockDocument)doc).atomicLock();
                }
                // check previous token
                Token<CppTokenId> token = CndTokenUtilities.shiftToNonWhiteBwd(doc, offset);
                if (token != null) {
                    switch (token.id()) {
                        case DOT:
                        case DOTMBR:
                        case ARROW:
                        case ARROWMBR:
                        case SCOPE:
                            kind = CsmReferenceKind.AFTER_DEREFERENCE_USAGE;
                    }
                }
            } finally {
                if (doc instanceof AtomicLockDocument) {
                    ((AtomicLockDocument)doc).atomicUnlock();
                }
            }
        }
        return kind;
    }
}
