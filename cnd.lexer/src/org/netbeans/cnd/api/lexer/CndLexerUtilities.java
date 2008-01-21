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
package org.netbeans.cnd.api.lexer;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Vladirmir Voskresensky
 */
public final class CndLexerUtilities {

    private CndLexerUtilities() {
    }

    public static TokenSequence<CppTokenId> getCppTokenSequence(final JTextComponent component, final int offset) {
        Document doc = component.getDocument();
        TokenHierarchy th = doc != null ? TokenHierarchy.get(doc) : null;
        TokenSequence<CppTokenId> ts = th != null ? getCppTokenSequence(th, offset) : null;
        return ts;
    }
    
    public static TokenSequence<CppTokenId> getCppTokenSequence(final TokenHierarchy hierarchy, final int offset) {
        if (hierarchy != null) {
            TokenSequence<?> ts = hierarchy.tokenSequence();
            while(ts != null && (offset == 0 || ts.moveNext())) {
                ts.move(offset);
                if (ts.language() == CppTokenId.languageC() ||
                        ts.language() == CppTokenId.languageCpp() ||
                        ts.language() == CppTokenId.languagePreproc()) {
                    return (TokenSequence<CppTokenId>)ts;
                }
                if (!ts.moveNext() && !ts.movePrevious()) {
                    return null;
                }
                ts = ts.embedded();
            }
        }
        return null;
    }
    
    public static boolean isCppIdentifierStart(char ch) {
        return Character.isJavaIdentifierStart(ch);
    }

    public static boolean isCppIdentifierStart(int codePoint) {
        return Character.isJavaIdentifierStart(codePoint);
    }

    public static boolean isCppIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
    }

    public static boolean isCppIdentifierPart(int codePoint) {
        return Character.isJavaIdentifierPart(codePoint);
    }

    public static CharSequence removeEscapedLF(CharSequence text, boolean escapedLF) {
        if (!escapedLF) {
            return text;
        } else {
            StringBuilder buffer = new StringBuilder();
            int lengthM1 = text.length() - 1;
            for (int i = 0; i <= lengthM1; i++) {
                char c = text.charAt(i);
                boolean append = true;
                if (c == '\\') { // check escaped LF
                    if ((i < lengthM1) && (text.charAt(i+1) == '\r')) {
                        i++;
                        append = false;
                    }
                    if ((i < lengthM1) && (text.charAt(i+1) == '\n')) {
                        i++;
                        append = false;
                    }                    
                }
                if (append) {
                    buffer.append(c);
                }
            }
            return buffer.toString();
        }
    }
    
    // filters
    private static Filter<CppTokenId> FILTER_STD_C;
    private static Filter<CppTokenId> FILTER_GCC_C;
    private static Filter<CppTokenId> FILTER_STD_CPP;
    private static Filter<CppTokenId> FILTER_PREPRPOCESSOR;

    public static Filter<CppTokenId> getDefatultFilter(boolean cpp) {
        return cpp ? getStdCppFilter() : getStdCFilter();
    }

    public synchronized static Filter<CppTokenId> getPreprocFilter() {
        if (FILTER_PREPRPOCESSOR == null) {
            FILTER_PREPRPOCESSOR = new Filter<CppTokenId>();
            addPreprocKeywords(FILTER_PREPRPOCESSOR);
        }
        return FILTER_PREPRPOCESSOR;
    }
    
    public synchronized static Filter<CppTokenId> getStdCFilter() {
        if (FILTER_STD_C == null) {
            FILTER_STD_C = new Filter<CppTokenId>();
            addCommonCCKeywords(FILTER_STD_C);
            addCOnlyKeywords(FILTER_STD_C);
        }
        return FILTER_STD_C;
    }

    public synchronized static Filter<CppTokenId> getGccCFilter() {
        if (FILTER_GCC_C == null) {
            FILTER_GCC_C = new Filter<CppTokenId>();
            addCommonCCKeywords(FILTER_GCC_C);
            addCOnlyKeywords(FILTER_GCC_C);
            addGccOnlyKeywords(FILTER_GCC_C);
        }
        return FILTER_GCC_C;
    }

    public synchronized static Filter<CppTokenId> getStdCppFilter() {
        if (FILTER_STD_CPP == null) {
            FILTER_STD_CPP = new Filter<CppTokenId>();
            addCommonCCKeywords(FILTER_STD_CPP);
            addCppOnlyKeywords(FILTER_STD_CPP);
        }
        return FILTER_STD_CPP;
    }

    ////////////////////////////////////////////////////////////////////////////
    // help methods
    
    private static void addPreprocKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.PREPROCESSOR_IF,
            CppTokenId.PREPROCESSOR_IFDEF,
            CppTokenId.PREPROCESSOR_IFNDEF,
            CppTokenId.PREPROCESSOR_ELSE,
            CppTokenId.PREPROCESSOR_ELIF,
            CppTokenId.PREPROCESSOR_ENDIF,
            CppTokenId.PREPROCESSOR_DEFINE,
            CppTokenId.PREPROCESSOR_UNDEF,
            CppTokenId.PREPROCESSOR_INCLUDE,
            CppTokenId.PREPROCESSOR_INCLUDE_NEXT,
            CppTokenId.PREPROCESSOR_LINE,
            CppTokenId.PREPROCESSOR_PRAGMA,
            CppTokenId.PREPROCESSOR_WARNING,
            CppTokenId.PREPROCESSOR_ERROR,
        };
        addToFilter(ids, filterToModify);        
    }
    
    private static void addCommonCCKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.AUTO,
            CppTokenId.BREAK,
            CppTokenId.CASE,
            CppTokenId.CHAR,
            CppTokenId.CONST,
            CppTokenId.CONTINUE,
            CppTokenId.DEFAULT,
            CppTokenId.DO,
            CppTokenId.DOUBLE,
            CppTokenId.ELSE,
            CppTokenId.ENUM,
            CppTokenId.EXTERN,
            CppTokenId.FLOAT,
            CppTokenId.FOR,
            CppTokenId.GOTO,
            CppTokenId.IF,
            CppTokenId.INT,
            CppTokenId.LONG,
            CppTokenId.REGISTER,
            CppTokenId.RETURN,
            CppTokenId.SHORT,
            CppTokenId.SIZNED,
            CppTokenId.SIZEOF,
            CppTokenId.STATIC,
            CppTokenId.STRUCT,
            CppTokenId.SWITCH,
            CppTokenId.TYPEDEF,
            CppTokenId.UNION,
            CppTokenId.UNSIGNED,
            CppTokenId.VOID,
            CppTokenId.VOLATILE,
            CppTokenId.WHILE,
        }; 
        addToFilter(ids, filterToModify);
    }

    private static  void addCppOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.ASM, // gcc and C++
            CppTokenId.BOOL, // C++
            CppTokenId.CATCH, //C++
            CppTokenId.CLASS, //C++
            CppTokenId.CONST_CAST, // C++
            CppTokenId.DELETE, // C++
            CppTokenId.DYNAMIC_CAST, // C++
            CppTokenId.EXPLICIT, // C++
            CppTokenId.EXPORT, // C++
            CppTokenId.FINALLY, //C++
            CppTokenId.FRIEND, // C++
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId.MUTABLE, // C++
            CppTokenId.NAMESPACE, //C++
            CppTokenId.NEW, //C++
            CppTokenId.OPERATOR, // C++
            CppTokenId.PRIVATE, //C++
            CppTokenId.PROTECTED, //C++
            CppTokenId.PUBLIC, // C++
            CppTokenId.REINTERPRET_CAST, //C++
            CppTokenId.STATIC_CAST, // C++
            CppTokenId.TEMPLATE, //C++
            CppTokenId.THIS, // C++
            CppTokenId.THROW, //C++
            CppTokenId.TRY, // C++
            CppTokenId.TYPEID, //C++
            CppTokenId.TYPENAME, //C++
            CppTokenId.TYPEOF, // gcc, C++
            CppTokenId.USING, //C++
            CppTokenId.VIRTUAL, //C++
            CppTokenId.WCHAR_T, // C++

            CppTokenId.TRUE, // C++
            CppTokenId.FALSE, // C++               
        };
        addToFilter(ids, filterToModify);
    }
    

    private static void addCOnlyKeywords(Filter<CppTokenId> filterToModify) {        
        CppTokenId[] ids = new CppTokenId[] {
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId.RESTRICT, // C
            CppTokenId._BOOL, // C 
            CppTokenId._COMPLEX, // C
            CppTokenId._IMAGINARY, // C                 
        };
        addToFilter(ids, filterToModify);
    }
    
    private static void addGccOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[] {
            CppTokenId.ASM, // gcc and C++
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId.TYPEOF, // gcc, C++
        };
        addToFilter(ids, filterToModify);        
    }
    
    private static void addToFilter(CppTokenId[] ids, Filter<CppTokenId> filterToModify) {
        for (CppTokenId id : ids) {
            assert id.fixedText() != null : "id " + id + " must have fixed text";
            filterToModify.addMatch(id.fixedText(), id);
        }
    }
}
