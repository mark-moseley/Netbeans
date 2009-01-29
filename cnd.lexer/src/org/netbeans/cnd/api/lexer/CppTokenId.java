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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.cnd.lexer.CppLexer;
import org.netbeans.modules.cnd.lexer.PreprocLexer;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of C/C++ languages defined as enum.
 *
 * @author Vladimir Voskresensky
 * @version 1.00
 */
public enum CppTokenId implements TokenId {
    
    // make sure token category names are the same used in the string
    // constants below
    
    ERROR(null, "error"), // NOI18N
    IDENTIFIER(null, "identifier"), // NOI18N

    // C/C++ keywords
    ALIGNOF("alignof", "keyword"), // g++ // NOI18N
    __ALIGNOF__("__alignof__", "keyword"), // gcc // NOI18N
    ASM("asm", "keyword-directive"), // gcc and C++ // NOI18N
    _ASM("_asm", "keyword"), // g++ // NOI18N
    __ASM("__asm", "keyword"), // gcc // NOI18N
    __ASM__("__asm__", "keyword"), // gcc // NOI18N
    AUTO("auto", "keyword"), // NOI18N
    BOOL("bool", "keyword"), // C++ // NOI18N
    BREAK("break", "keyword-directive"), // NOI18N
    CASE("case", "keyword-directive"), // NOI18N
    CATCH("catch", "keyword-directive"), // C++ // NOI18N
    CHAR("char", "keyword"), // NOI18N
    CLASS("class", "keyword"), // C++ // NOI18N
    CONST("const", "keyword"), // NOI18N
    __CONST("__const", "keyword"), // gcc // NOI18N
    __CONST__("__const__", "keyword"), // gcc C only // NOI18N
    CONST_CAST("const_cast", "keyword"), // C++ // NOI18N
    CONTINUE("continue", "keyword-directive"), // NOI18N
    DEFAULT("default", "keyword-directive"), // NOI18N
    DELETE("delete", "keyword"), // C++ // NOI18N
    DO("do", "keyword-directive"), // NOI18N
    DOUBLE("double", "keyword"), // NOI18N
    DYNAMIC_CAST("dynamic_cast", "keyword"), // C++ // NOI18N
    ELSE("else", "keyword-directive"), // NOI18N
    ENUM("enum", "keyword"), // NOI18N
    EXPLICIT("explicit", "keyword"), // C++ // NOI18N
    EXPORT("export", "keyword"), // C++ // NOI18N
    EXTERN("extern", "keyword"), // NOI18N
    FINALLY("finally", "keyword-directive"), // C++ // NOI18N
    FLOAT("float", "keyword"), // NOI18N
    FOR("for", "keyword-directive"), // NOI18N
    FRIEND("friend", "keyword"), // C++ // NOI18N
    __FUNC__("__func__", "keyword"), // NOI18N
    GOTO("goto", "keyword-directive"), // NOI18N
    IF("if", "keyword-directive"), // NOI18N
    INLINE("inline", "keyword"), // now in C also // NOI18N
    _INLINE("_inline", "keyword"), // g++ // NOI18N
    __INLINE("__inline", "keyword"), // gcc // NOI18N
    __INLINE__("__inline__", "keyword"), // gcc // NOI18N
    INT("int", "keyword"), // NOI18N
    LONG("long", "keyword"), // NOI18N
    MUTABLE("mutable", "keyword"), // C++ // NOI18N
    NAMESPACE("namespace", "keyword"), // C++ // NOI18N
    NEW("new", "keyword"), // C++ // NOI18N
    OPERATOR("operator", "keyword"), // C++ // NOI18N
    PASCAL("pascal", "keyword"), // g++ // NOI18N
    _PASCAL("_pascal", "keyword"), // g++ // NOI18N
    __PASCAL("__pascal", "keyword"), // g++ // NOI18N
    PRIVATE("private", "keyword"), //C++ // NOI18N
    PROTECTED("protected", "keyword"), //C++ // NOI18N
    PUBLIC("public", "keyword"), // C++ // NOI18N
    REGISTER("register", "keyword"), // NOI18N
    REINTERPRET_CAST("reinterpret_cast", "keyword"), //C++ // NOI18N
    RESTRICT("restrict", "keyword"), // C // NOI18N
    RETURN("return", "keyword-directive"), // NOI18N
    SHORT("short", "keyword"), // NOI18N
    SIGNED("signed", "keyword"), // NOI18N
    __SIGNED("__signed", "keyword"), // gcc // NOI18N
    __SIGNED__("__signed__", "keyword"), // gcc // NOI18N
    SIZEOF("sizeof", "keyword"), // NOI18N
    STATIC("static", "keyword"), // NOI18N
    STATIC_CAST("static_cast", "keyword"), // C++ // NOI18N
    STRUCT("struct", "keyword"), // NOI18N
    SWITCH("switch", "keyword-directive"), // NOI18N
    TEMPLATE("template", "keyword"), // C++ // NOI18N
    THIS("this", "keyword"), // C++ // NOI18N
    THROW("throw", "keyword-directive"), //C++ // NOI18N
    TRY("try", "keyword-directive"), // C++ // NOI18N
    TYPEDEF("typedef", "keyword"), // NOI18N
    TYPEID("typeid", "keyword"), // C++ // NOI18N
    TYPENAME("typename", "keyword"), // C++ // NOI18N
    TYPEOF("typeof", "keyword"), // gcc, C++ // NOI18N
    __TYPEOF("__typeof", "keyword"), // gcc // NOI18N
    __TYPEOF__("__typeof__", "keyword"), // gcc // NOI18N
    UNION("union", "keyword"), // NOI18N
    UNSIGNED("unsigned", "keyword"), // NOI18N
    __UNSIGNED__("__unsigned__", "keyword"), // g++ // NOI18N
    USING("using", "keyword"), //C++ // NOI18N
    VIRTUAL("virtual", "keyword"), //C++ // NOI18N
    VOID("void", "keyword"), // NOI18N
    VOLATILE("volatile", "keyword"), // NOI18N
    __VOLATILE("__volatile", "keyword"), // gcc // NOI18N
    __VOLATILE__("__volatile__", "keyword"), // gcc // NOI18N
    WCHAR_T("wchar_t", "keyword"), // C++ // NOI18N
    WHILE("while", "keyword-directive"), // NOI18N
    __ATTRIBUTE__("__attribute__", "keyword"), // gcc // NOI18N
    __ATTRIBUTE("__attribute", "keyword"), // gcc // NOI18N
    _BOOL("_Bool", "keyword"), // C // NOI18N
    _CDECL("_cdecl", "keyword"), // g++ // NOI18N
    __CDECL("__cdecl", "keyword"), // g++ // NOI18N
    _COMPLEX("_Complex", "keyword"), // C // NOI18N
    __COMPLEX__("__complex__", "keyword"), // gcc // NOI18N
    _DECLSPEC("_declspec", "keyword"), // g++ // NOI18N
    __DECLSPEC("__declspec", "keyword"), // g++ // NOI18N
    __EXTENSION__("__extension__", "keyword"), // g++ // NOI18N
    _FAR("_far", "keyword"), // g++ // NOI18N
    __FAR("__far", "keyword"), // g++ // NOI18N
    _IMAGINARY("_Imaginary", "keyword"), // C // NOI18N
    __IMAG__("__imag__", "keyword"), // gcc // NOI18N
    _INT64("_int64", "keyword"), // g++ // NOI18N
    __INT64("__int64", "keyword"), // g++ // NOI18N
    __INTERRUPT("__interrupt", "keyword"), // g++ // NOI18N
    _NEAR("_near", "keyword"), // g++ // NOI18N
    __NEAR("__near", "keyword"), // g++ // NOI18N
    __REAL__("__real__", "keyword"), // gcc // NOI18N
    __RESTRICT("__restrict", "keyword"), // g++ // NOI18N
    _STDCALL("_stdcall", "keyword"), // g++ // NOI18N
    __STDCALL("__stdcall", "keyword"), // g++ // NOI18N
    __THREAD("__thread", "keyword"), // gcc // NOI18N
    __UNUSED__("__unused__", "keyword"), // gcc // NOI18N
    __W64("__w64", "keyword"), // g++ // NOI18N

    INT_LITERAL(null, "number"), // NOI18N
    LONG_LITERAL(null, "number"), // NOI18N
    LONG_LONG_LITERAL(null, "number"), // NOI18N
    FLOAT_LITERAL(null, "number"), // NOI18N
    DOUBLE_LITERAL(null, "number"), // NOI18N
    UNSIGNED_LITERAL(null, "number"), // NOI18N
    UNSIGNED_LONG_LITERAL(null, "number"), // NOI18N
    UNSIGNED_LONG_LONG_LITERAL(null, "number"), // NOI18N
    CHAR_LITERAL(null, "character"), // NOI18N
    STRING_LITERAL(null, "string"), // NOI18N
    
    TRUE("true", "literal"), // C++ // NOI18N
    FALSE("false", "literal"), // C++ // NOI18N
    NULL("null", "literal"), // NOI18N
    
    LPAREN("(", "separator"), // NOI18N
    RPAREN(")", "separator"), // NOI18N
    LBRACE("{", "separator"), // NOI18N
    RBRACE("}", "separator"), // NOI18N
    LBRACKET("[", "separator"), // NOI18N
    RBRACKET("]", "separator"), // NOI18N
    SEMICOLON(";", "separator"), // NOI18N
    COMMA(",", "separator"), // NOI18N
    DOT(".", "separator"), // NOI18N
    DOTMBR(".*", "separator"), // NOI18N
    SCOPE("::", "separator"), // NOI18N
    ARROW("->", "separator"), // NOI18N
    ARROWMBR("->*", "separator"), // NOI18N
    
    EQ("=", "operator"), // NOI18N
    GT(">", "operator"), // NOI18N
    LT("<", "operator"), // NOI18N
    NOT("!", "operator"), // NOI18N
    TILDE("~", "operator"), // NOI18N
    QUESTION("?", "operator"), // NOI18N
    COLON(":", "operator"), // NOI18N
    EQEQ("==", "operator"), // NOI18N
    LTEQ("<=", "operator"), // NOI18N
    GTEQ(">=", "operator"), // NOI18N
    NOTEQ("!=","operator"), // NOI18N
    AMPAMP("&&", "operator"), // NOI18N
    BARBAR("||", "operator"), // NOI18N 
    PLUSPLUS("++", "operator"), // NOI18N 
    MINUSMINUS("--","operator"), // NOI18N
    PLUS("+", "operator"), // NOI18N
    MINUS("-", "operator"), // NOI18N
    STAR("*", "operator"), // NOI18N
    SLASH("/", "operator"), // NOI18N
    AMP("&", "operator"), // NOI18N
    BAR("|", "operator"), // NOI18N
    CARET("^", "operator"), // NOI18N
    PERCENT("%", "operator"), // NOI18N
    LTLT("<<", "operator"), // NOI18N
    GTGT(">>", "operator"), // NOI18N
    PLUSEQ("+=", "operator"), // NOI18N
    MINUSEQ("-=", "operator"), // NOI18N
    STAREQ("*=", "operator"), // NOI18N
    SLASHEQ("/=", "operator"), // NOI18N
    AMPEQ("&=", "operator"), // NOI18N
    BAREQ("|=", "operator"), // NOI18N
    CARETEQ("^=", "operator"), // NOI18N
    PERCENTEQ("%=", "operator"), // NOI18N
    LTLTEQ("<<=", "operator"), // NOI18N
    GTGTEQ(">>=", "operator"), // NOI18N
    
    ELLIPSIS("...", "special"), // NOI18N
    AT("@", "special"), // NOI18N
    DOLLAR("$", "special"), // NOI18N
    SHARP("#", "special"), // NOI18N
    DBL_SHARP("##", "special"), // NOI18N
    BACK_SLASH("\\", "special"), // NOI18N
            
    WHITESPACE(null, "whitespace"), // NOI18N // all spaces except new line
    ESCAPED_LINE(null, "whitespace"), // NOI18N // line escape with \
    ESCAPED_WHITESPACE(null, "whitespace"), // NOI18N // whitespace escape with \ inside it
    NEW_LINE(null, "whitespace"), // NOI18N // new line \n or \r
    LINE_COMMENT(null, "comment"), // NOI18N
    BLOCK_COMMENT(null, "comment"), // NOI18N
    DOXYGEN_COMMENT(null, "comment"), // NOI18N
    
    // Prerpocessor 
    //   - on top level
    PREPROCESSOR_DIRECTIVE(null, "preprocessor"), // NOI18N
    //   - tokens
    PREPROCESSOR_START("#", "preprocessor"), // NOI18N
    PREPROCESSOR_IF("if", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IFDEF("ifdef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IFNDEF("ifndef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ELSE("else", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ELIF("elif", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ENDIF("endif", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_DEFINE("define", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_UNDEF("undef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_INCLUDE("include", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_INCLUDE_NEXT("include_next", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_LINE("line", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IDENT("ident", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_PRAGMA("pragma", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_WARNING("warning", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ERROR("error", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_DEFINED("defined", "preprocessor-keyword"), // NOI18N
    
    PREPROCESSOR_USER_INCLUDE(null, "preprocessor-user-include-literal"), // NOI18N
    PREPROCESSOR_SYS_INCLUDE(null, "preprocessor-system-include-literal"), // NOI18N
    PREPROCESSOR_IDENTIFIER(null, "preprocessor-identifier"), // NOI18N
    
    // Errors
    INVALID_COMMENT_END("*/", "error"), // NOI18N
    FLOAT_LITERAL_INVALID(null, "number"); // NOI18N
    
    // make sure string names are the same used in the tokenIds above
    public static final String IDENTIFIER_CATEGORY = "identifier"; // NOI18N
    public static final String WHITESPACE_CATEGORY = "whitespace"; // NOI18N
    public static final String COMMENT_CATEGORY = "comment"; // NOI18N
    public static final String KEYWORD_CATEGORY = "keyword"; // NOI18N
    public static final String KEYWORD_DIRECTIVE_CATEGORY = "keyword-directive"; // NOI18N
    public static final String ERROR_CATEGORY = "error"; // NOI18N
    public static final String NUMBER_CATEGORY = "number"; // NOI18N
    public static final String LITERAL_CATEGORY = "literal"; // NOI18N
    public static final String CHAR_CATEGORY = "character"; // NOI18N
    public static final String STRING_CATEGORY = "string"; // NOI18N
    public static final String SEPARATOR_CATEGORY = "separator"; // NOI18N
    public static final String OPERATOR_CATEGORY = "operator"; // NOI18N
    public static final String SPECIAL_CATEGORY = "special"; // NOI18N
    public static final String PREPROCESSOR_CATEGORY = "preprocessor"; // NOI18N
    public static final String PREPROCESSOR_KEYWORD_CATEGORY = "preprocessor-keyword"; // NOI18N
    public static final String PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY = "preprocessor-keyword-directive"; // NOI18N
    public static final String PREPROCESSOR_IDENTIFIER_CATEGORY = "preprocessor-identifier"; // NOI18N
    public static final String PREPROCESSOR_USER_INCLUDE_CATEGORY = "preprocessor-user-include-literal"; // NOI18N
    public static final String PREPROCESSOR_SYS_INCLUDE_CATEGORY = "preprocessor-system-include-literal"; // NOI18N
  
    private final String fixedText;

    private final String primaryCategory;

    CppTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<CppTokenId> languageHeader;
    private static final Language<CppTokenId> languageC;
    private static final Language<CppTokenId> languageCpp;
    private static final Language<CppTokenId> languagePreproc;
    
    static {
        languageHeader = CppHierarchy.createHeaderLanguage();
        languageC = CppHierarchy.createCLanguage();
        languageCpp = CppHierarchy.createCppLanguage();
        languagePreproc = CppHierarchy.createPreprocLanguage();
    }

    public static Language<CppTokenId> languageC() {
        return languageC;
    }

    public static Language<CppTokenId> languageCpp() {
        return languageCpp;
    }
    
    public static Language<CppTokenId> languagePreproc() {
        return languagePreproc;
    }

    public static Language<CppTokenId> languageHeader() {
        return languageHeader;
    }
    
    private static final class CppHierarchy extends LanguageHierarchy<CppTokenId> {
        private final String mimeType;
        private CppHierarchy(String mimeType) {
            this.mimeType = mimeType;
        }

        private static Language<CppTokenId> createHeaderLanguage() {
            return new CppHierarchy(MIMENames.HEADER_MIME_TYPE).language();
        }
        
        private static Language<CppTokenId> createCppLanguage() {
            return new CppHierarchy(MIMENames.CPLUSPLUS_MIME_TYPE).language();
        }
        
        private static Language<CppTokenId> createCLanguage() {
            return new CppHierarchy(MIMENames.C_MIME_TYPE).language();
        }
        
        private static Language<CppTokenId> createPreprocLanguage() {
            return new CppHierarchy(MIMENames.PREPROC_MIME_TYPE).language();
        }
        
        @Override
        protected String mimeType() {
            return mimeType;
        }

        @Override
        protected Collection<CppTokenId> createTokenIds() {
            return EnumSet.allOf(CppTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<CppTokenId>> createTokenCategories() {
            Map<String,Collection<CppTokenId>> cats = new HashMap<String,Collection<CppTokenId>>();
            // Additional literals being a lexical error
            cats.put(ERROR_CATEGORY, EnumSet.of(
                CppTokenId.FLOAT_LITERAL_INVALID
            ));
            // Literals category
            EnumSet<CppTokenId> l = EnumSet.of(
                CppTokenId.INT_LITERAL,
                CppTokenId.LONG_LITERAL,
                CppTokenId.LONG_LONG_LITERAL,
                CppTokenId.FLOAT_LITERAL,
                CppTokenId.DOUBLE_LITERAL,
                CppTokenId.UNSIGNED_LITERAL,
                CppTokenId.CHAR_LITERAL,
                CppTokenId.STRING_LITERAL
            );
            cats.put(LITERAL_CATEGORY, l);
        
            return cats;
        }

        @Override
        protected Lexer<CppTokenId> createLexer(LexerRestartInfo<CppTokenId> info) {
            if (MIMENames.PREPROC_MIME_TYPE.equals(this.mimeType)) {
                return new PreprocLexer(CndLexerUtilities.getDefatultFilter(true), info);
            } else if (MIMENames.C_MIME_TYPE.equals(this.mimeType)) {
                return new CppLexer(CndLexerUtilities.getDefatultFilter(false), info);
            } else { // for header and C++
                return new CppLexer(CndLexerUtilities.getDefatultFilter(true), info);
            }
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<CppTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment and string literal
            switch (token.id()) {
                case DOXYGEN_COMMENT:
                    return LanguageEmbedding.create(DoxygenTokenId.language(), 3,
                            (token.partType() == PartType.COMPLETE) ? 2 : 0);
                case STRING_LITERAL:
                    return LanguageEmbedding.create(CppStringTokenId.languageDouble(), 0, 0);
                case CHAR_LITERAL:
                    return LanguageEmbedding.create(CppStringTokenId.languageSingle(), 0, 0);
                case PREPROCESSOR_DIRECTIVE:
                    return LanguageEmbedding.create(languagePreproc, 0, 0);
            }
            return null; // No embedding
        }
    }
}
