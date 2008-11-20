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
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.completion.cplusplus.CsmFinderFactory;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class CompletionSupport {

    private final Reference<Document> docRef;
    // not for external instantiation

    private CompletionSupport(Document doc) {
        docRef = new WeakReference<Document>(doc);
    }

    public static CompletionSupport get(JTextComponent component) {
        return get(component.getDocument());
    }

    public static CompletionSupport get(final Document doc) {
        CompletionSupport support = (CompletionSupport) doc.getProperty(CompletionSupport.class);
        if (support == null) {
            // for now accept only documents with known languages
            boolean valid = (CndLexerUtilities.getLanguage(doc) != null);
            if (valid) {
                support = new CompletionSupport(doc);
                doc.putProperty(CompletionSupport.class, support);
//                synchronized (doc) {
//                    support = (CompletionSupport) doc.getProperty(CompletionSupport.class);
//                    if (support == null) {
//                        doc.putProperty(CompletionSupport.class, support = new CompletionSupport(doc));
//                    }
//                }
            }
        }
        return support;
    }

    public final Document getDocument() {
        return this.docRef.get();
    }

    public static boolean isPreprocCompletionEnabled(Document doc, int offset) {
        return isIncludeCompletionEnabled(doc, offset);
    }

    public static boolean isIncludeCompletionEnabled(Document doc, int offset) {
        TokenSequence<CppTokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, false, true);
        if (ts == null) {
            return false;
        }
        if (ts.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<CppTokenId> embedded = (TokenSequence<CppTokenId>) ts.embedded();
            if (CndTokenUtilities.moveToPreprocKeyword(embedded)) {
                switch (embedded.token().id()) {
                    case PREPROCESSOR_INCLUDE:
                    case PREPROCESSOR_INCLUDE_NEXT:
                        // completion enabled after #include(_next) keywords
                        return (embedded.offset() + embedded.token().length()) <= offset;
                }
            }
        }
        return false;
    }

    public final CsmFinder getFinder() {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        assert dobj != null;
        FileObject fo = dobj.getPrimaryFile();
        return CsmFinderFactory.getDefault().getFinder(fo);
    }
    private static final char[] COMMAND_SEPARATOR_CHARS = new char[]{
        ';', '{', '}'
    };
    private int lastSeparatorOffset = -1;

    protected void setLastSeparatorOffset(int lastSeparatorOffset) {
        this.lastSeparatorOffset = lastSeparatorOffset;
    }

    /** Return the position of the last command separator before
     * the given position.
     */
    protected int getLastCommandSeparator(final int pos) throws BadLocationException {
        if (pos < 0 || pos > getDocument().getLength()) {
            throw new BadLocationException("position is out of range[" + 0 + "-" + getDocument().getLength() + "]", pos); // NOI18N
        }
        if (pos == 0) {
            return 0;
        }
        if (lastSeparatorOffset >= 0 && lastSeparatorOffset < pos) {
            return lastSeparatorOffset;
        }
        lastSeparatorOffset = CndTokenUtilities.getLastCommandSeparator(getDocument(), pos);
        return lastSeparatorOffset;
    }

    /** Get the class from name. The import sections are consulted to find
     * the proper package for the name. If the search in import sections fails
     * the method can ask the finder to search just by the given name.
     * @param className name to resolve. It can be either the full name
     *   or just the name without the package.
     * @param searchByName if true and the resolving through the import sections fails
     *   the finder is asked to find the class just by the given name
     */
    public static CsmClassifier getClassFromName(CsmFinder finder, String className, boolean searchByName) {
        // XXX handle primitive type
        CsmClassifier ret = null;
//        CsmClass ret = JavaCompletion.getPrimitiveClass(className);
//        if (ret == null) {
//
//            ret = getIncludeProc().getClassifier(className);
//        }
        if (ret == null && searchByName) {
            List clsList = finder.findClasses(null, className, true, false);
            if (clsList != null && clsList.size() > 0) {
                if (!clsList.isEmpty()) { // more matching classes
                    ret = (CsmClassifier) clsList.get(0); // get the first one
                }
            }

        }
        return ret;
    }

    /** Get the class that belongs to the given position */
    public CsmClass getClass(int pos) {
        return CompletionUtilities.findClassOnPosition(getDocument(), pos);
    }

    /** Get the class or function definition that belongs to the given position */
    public CsmOffsetableDeclaration getDefinition(int pos, FileReferencesContext fileContext) {
        return CompletionUtilities.findFunDefinitionOrClassOnPosition(getDocument(), pos, fileContext);
    }

    public boolean isStaticBlock(int pos) {
        return false;
    }

    public int[] getFunctionBlock(int[] identifierBlock) throws BadLocationException {
//        int[] retValue = super.getFunctionBlock(identifierBlock);
//        return retValue;
        return null;
    }

    public static boolean isAssignable(CsmType from, CsmType to) {
        CsmClassifier fromCls = from.getClassifier();
        CsmClassifier toCls = to.getClassifier();

        if (fromCls == null) {
            return false;
        }

        if (toCls == null) {
            return false;
        }

        // XXX review!
        if (fromCls.equals(CsmCompletion.NULL_CLASS)) {
            return to.getArrayDepth() > 0 || !CsmCompletion.isPrimitiveClass(toCls);
        }

        if (toCls.equals(CsmCompletion.OBJECT_CLASS)) { // everything is object
            return (from.getArrayDepth() > to.getArrayDepth()) || (from.getArrayDepth() == to.getArrayDepth() && !CsmCompletion.isPrimitiveClass(fromCls));
        }

        if (from.getArrayDepth() != to.getArrayDepth() ||
                from.getPointerDepth() != to.getPointerDepth()) {
            return false;
        }

        if (fromCls.equals(toCls)) {
            return true; // equal classes
        }
        String tfrom = from.getCanonicalText().toString().replaceAll("const", "").trim(); // NOI18N
        String tto = to.getCanonicalText().toString().replaceAll("const", "").trim(); // NOI18N

        if (tfrom.equals(tto)) {
            return true;
        }
        return false;
    }

    public CsmType getCommonType(CsmType typ1, CsmType typ2) {
        if (typ1.equals(typ2)) {
            return typ1;
        }

        // The following part
        if (!CndLexerUtilities.isType(typ1.getClassifier().getName().toString()) && ! !CndLexerUtilities.isType(typ2.getClassifier().getName().toString())) { // non-primitive classes
            if (isAssignable(typ1, typ2)) {
                return typ1;
            } else if (isAssignable(typ2, typ1)) {
                return typ2;
            } else {
                return null;
            }
        } else { // at least one primitive class
            if (typ1.getArrayDepth() != typ2.getArrayDepth()) {
                return null;
            }
            return null;
        }
    }

    /** Filter the list of the methods (usually returned from
     * Finder.findMethods()) or the list of the constructors
     * by the given parameter specification.
     * @param methodList list of the methods. They should have the same
     *   name but in fact they don't have to.
     * @param parmTypes parameter types specification. If set to null, no filtering
     *   is performed and the same list is returned. If a particular
     * @param acceptMoreParameters useful for code completion to get
     *   even the methods with more parameters.
     */
    public static List<CsmFunction> filterMethods(List<CsmFunction> methodList, List parmTypeList,
            boolean acceptMoreParameters) {
        assert (methodList != null);
        if (parmTypeList == null) {
            return methodList;
        }

        List<CsmFunction> ret = new ArrayList<CsmFunction>();
        int parmTypeCnt = parmTypeList.size();
        int cnt = methodList.size();
        int maxMatched = -1;
        for (int i = 0; i < cnt; i++) {
            // Use constructor conversion to allow to use it too for the constructors
            CsmFunction m = methodList.get(i);
            @SuppressWarnings("unchecked")
            CsmParameter[] methodParms = (CsmParameter[]) m.getParameters().toArray(new CsmParameter[m.getParameters().size()]);
            if (methodParms.length == parmTypeCnt || (acceptMoreParameters && methodParms.length >= parmTypeCnt)) {
                boolean accept = true;
                boolean bestMatch = !acceptMoreParameters;
                int matched = 0;
                for (int j = 0; accept && j < parmTypeCnt; j++) {
                    if (methodParms[j] == null) {
                        System.err.println("Null parameter " + j + " in function " + m.getUID()); //NOI18N
                        bestMatch = false;
                        continue;
                    }
                    CsmType mpt = methodParms[j].getType();
                    CsmType t = (CsmType) parmTypeList.get(j);
                    if (t != null) {
                        if (!methodParms[j].isVarArgs() && !equalTypes(t, mpt)) {
                            bestMatch = false;
                            if (!isAssignable(t, mpt)) {
                                accept = false;
                            // TODO: do not break now, count matches
                            // break;
                            } else {
                                matched++;
                            }
                        } else {
                            matched++;
                        }
                    } else { // type in list is null
                        bestMatch = false;
                    }
                }

                if (accept) {
                    if (bestMatch) {
                        ret.clear();
                    } else if (matched > maxMatched) {
                        maxMatched = matched;
                        ret.clear();
                    }
                    ret.add(m);
                    if (bestMatch) {
                        break;
                    }
                } else {
                    if (matched > maxMatched) {
                        maxMatched = matched;
                        ret.clear();
                        ret.add(m);
                    }
                }

            } else if (methodParms.length == 0 && parmTypeCnt == 1) { // for cases like f(void)
                CsmType t = (CsmType) parmTypeList.get(0);
                if (t != null && "void".equals(t.getText())) { // best match // NOI18N
                    ret.clear();
                    ret.add(m);
                }
            }
        }
        return ret;
    }

    ////////////////////////////////////////////////
    // overriden functions to resolve expressions
    /////////////////////////////////////////////////

    // utitlies

//    public TokenItem getTokenItem(int offset) {
//        TokenItem token;
//        try {
//            int checkOffset = offset;
//            if (offset == getDocument().getLength()) {
//                if (offset == 0) {
//                    return null;
//                }
//                checkOffset--;
//            }
//            token = getTokenChain(checkOffset, checkOffset + 1);
//        } catch (BadLocationException e) {
//            token = null;
//        }
//        return token;
//    }
//
//    public TokenItem shiftToNonWhiteBwd(TokenItem token) {
//        if (token == null) {
//            return null;
//        }
//        boolean checkedFirst = false;
//        do {
//            switch (token.getCppTokenId().getNumericID()) {
//                case CppTokenId.WHITESPACE_ID:
//                    if (checkedFirst) {
//                        if (token.getImage().contains("\n")) { // NOI18N
//                            return null;
//                        }
//                    }
//                    break;
//                case CppTokenId.BLOCK_COMMENT_ID:
//                    // skip
//                    break;
//                default:
//                    return token;
//            }
//            token = token.getPrevious();
//            checkedFirst = true;
//        } while (token != null);
//        return null;
//    }

//    private static final CppTokenId[] BRACKET_SKIP_TOKENS = new CppTokenId[]{
//        CppTokenId.LINE_COMMENT,
//        CppTokenId.BLOCK_COMMENT,
//        CppTokenId.CHAR_LITERAL,
//        CppTokenId.STRING_LITERAL
//    };
//
//    // tokens valid for include-completion provider
//    private static final CppTokenId[] INCLUDE_COMPLETION_TOKENS = new CppTokenId[]{
//        CppTokenId.PREPROCESSOR_USER_INCLUDE,
//        CppTokenId.PREPROCESSOR_SYS_INCLUDE,
//    };
    public static boolean isCompletionEnabled(Document doc, int offset) {
        if (doc.getLength() == 0) {
            // it's fine to show completion in empty doc
            return true;
        }
        TokenSequence<CppTokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, true, offset > 0);
        if (ts == null) {
            return false;
        }
        if (ts.offset() < offset && offset <= ts.offset() + ts.token().length()) {
            // completion is disabled in some tokens
            switch (ts.token().id()) {
                case LINE_COMMENT:
                case CHAR_LITERAL:
                case STRING_LITERAL:
                case PREPROCESSOR_USER_INCLUDE:
                case PREPROCESSOR_SYS_INCLUDE:
                case PREPROCESSOR_DEFINED:
                    return false;
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                    // ok after end of token
                    return offset == ts.offset() + ts.token().length();
            }
            // main completion is not responsible 
            if (CppTokenId.PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY.equals(ts.token().id().primaryCategory())) {
                return false;
            }
            if (CppTokenId.NUMBER_CATEGORY.equals(ts.token().id().primaryCategory())) {
                return false;
            }
        }
        return true;
    }

    public static boolean needShowCompletionOnText(JTextComponent target, String typedText) throws BadLocationException {
        char typedChar = typedText.charAt(typedText.length() - 1);
        if (typedChar == ' ' || typedChar == '>' || typedChar == ':' || typedChar == '.' || typedChar == '*') {
            int dotPos = target.getCaret().getDot();
            Document doc = target.getDocument();
            TokenSequence<CppTokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, dotPos, true, true);
            if (ts == null) {
                return false;
            }
            Token<CppTokenId> token = ts.token();
            if (!ts.movePrevious()) {
                return false;
            }
            switch (token.id()) {
                case WHITESPACE:
                    return token.length() == 1 && (ts.token().id() == CppTokenId.NEW);
                case ARROW:
                    return true;
                case DOT:
                    return true;
                case ARROWMBR:
                case DOTMBR:
                    return true;
                case SCOPE:
                    return true;
            }
        }
        return false;
    }

    private static boolean equalTypes(CsmType t, CsmType mpt) {
        assert t != null;
        if (t.equals(mpt)) {
            return true;
        } else if (mpt != null) {
            String t1 = t.getCanonicalText().toString();
            String t2 = mpt.getCanonicalText().toString();
            return t1.equals(t2);
        }
        return false;
    }
}
