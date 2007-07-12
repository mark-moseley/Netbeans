/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.utils;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.impl.support.APTCommentToken;
import org.netbeans.modules.cnd.apt.impl.support.APTConstTextToken;
import org.netbeans.modules.cnd.apt.impl.support.APTTestToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;

/**
 * APT utilities
 * @author Vladimir Voskresensky
 */
public class APTUtils {
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.apt"); // NOI18N
    
    static {
        // command line param has priority for logging
        String level = System.getProperty("org.netbeans.modules.cnd.apt.level"); // NOI18N
        // do not change it
        if (level == null) {
            // command line param has priority for logging
            if (APTTraceFlags.TRACE_APT | APTTraceFlags.TRACE_APT_LEXER) {
                LOG.setLevel(Level.ALL);
            } else {
                LOG.setLevel(Level.SEVERE);
            }
        } else {
            LOG.setLevel(Level.parse(level));
        }
    }
    
    /** Creates a new instance of APTUtils */
    private APTUtils() {
    }
    
    public static APTToken createAPTToken(int type) {
        // Preprocessor tokens can be made constText, but we can get '#define' and '# define'
        // which have different text. so for now they are treated as usual tokens
        if (isPreprocessorToken(type)) {
            return APTTraceFlags.USE_APT_TEST_TOKEN ? (APTToken)new APTTestToken() : new APTBaseToken();
        }
        switch (type) {
            // IDs
            case APTTokenTypes.ID:
            case APTTokenTypes.ID_DEFINED:
                // Strings and chars
            case APTTokenTypes.STRING_LITERAL:
            case APTTokenTypes.CHAR_LITERAL:
                // Numbers
            case APTTokenTypes.DECIMALINT:
            case APTTokenTypes.HEXADECIMALINT:
            case APTTokenTypes.FLOATONE:
            case APTTokenTypes.FLOATTWO:
            case APTTokenTypes.OCTALINT:
            case APTTokenTypes.NUMBER:
                // Include strings
            case APTTokenTypes.INCLUDE_STRING:
            case APTTokenTypes.SYS_INCLUDE_STRING:
                //Other
            case APTTokenTypes.END_PREPROC_DIRECTIVE:
                return APTTraceFlags.USE_APT_TEST_TOKEN ? (APTToken)new APTTestToken() : new APTBaseToken();
                
                // Comments
            case APTTokenTypes.CPP_COMMENT:
            case APTTokenTypes.COMMENT:
                return new APTCommentToken();
                
            default: /*assert(APTConstTextToken.constText[type] != null) : "Do not know text for constText token of type " + type;  // NOI18N*/
                return new APTConstTextToken();
        }
    }
    
    public static String toString(TokenStream ts) {
        StringBuilder retValue = new StringBuilder();
        try {
            for (Token token = ts.nextToken();!isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.toString());
                
                token=ts.nextToken();
                
                if (!isEOF(token)) {
                    retValue.append(" "); // NOI18N
                }
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.SEVERE, "error on converting token stream to text", ex); // NOI18N
        }
        return retValue.toString();
    }
    
    public static String stringize(TokenStream ts) {
        StringBuilder retValue = new StringBuilder();
        try {
            for (APTToken token = (APTToken)ts.nextToken();!isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.getText());
                APTToken next =(APTToken)ts.nextToken();
                if (!isEOF(next)) {
                    // if tokens were without spaces => no space
                    // if were with spaces => insert only one space
                    retValue.append(next.getOffset() == token.getEndOffset() ? "" : ' ');// NOI18N
                }
                token = next;
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.SEVERE, "error on stringizing token stream", ex); // NOI18N
        }
        return retValue.toString();
    }
    
    public static String macros2String(Map<String/*getTokenTextKey(token)*/, APTMacro> macros) {
        StringBuilder retValue = new StringBuilder();
        retValue.append("MACROS (sorted "+macros.size()+"):\n"); // NOI18N
        List<String> macrosSorted = new ArrayList<String>(macros.keySet());
        Collections.sort(macrosSorted);
        for (String key : macrosSorted) {
            APTMacro macro = macros.get(APTUtils.getTokenTextKey(new APTBaseToken(key)));
            assert(macro != null);
            retValue.append(macro);
            retValue.append("'\n"); // NOI18N
        }
        return retValue.toString();
    }
    
    public static String includes2String(List<String> includePaths) {
        StringBuilder retValue = new StringBuilder();
        for (Iterator<String> it = includePaths.iterator(); it.hasNext();) {
            String path = it.next();
            retValue.append(path);
            if (it.hasNext()) {
                retValue.append('\n'); // NOI18N
            }
        }
        return retValue.toString();
    }
    
    public static boolean isPreprocessorToken(Token token) {
        assert (token != null);
        return isPreprocessorToken(token.getType());
    }
    
    public static boolean isPreprocessorToken(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.PREPROC_DIRECTIVE:
            case APTTokenTypes.INCLUDE:
            case APTTokenTypes.INCLUDE_NEXT:
            case APTTokenTypes.DEFINE:
            case APTTokenTypes.UNDEF:
            case APTTokenTypes.IFDEF:
            case APTTokenTypes.IFNDEF:
            case APTTokenTypes.IF:
            case APTTokenTypes.ELIF:
            case APTTokenTypes.ELSE:
            case APTTokenTypes.ENDIF:
            case APTTokenTypes.PRAGMA:
            case APTTokenTypes.LINE:
            case APTTokenTypes.ERROR:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isID(Token token) {
        return token != null && token.getType() == APTTokenTypes.ID;
    }
    
    public static boolean isEOF(Token token) {
        assert (token != null);
        return token == null || isEOF(token.getType());
    }
    
    public static boolean isEOF(int ttype) {
        return ttype == APTTokenTypes.EOF;
    }
    
    public static boolean isVaArgsToken(Token token) {
        return token != null && token.getText().equals(VA_ARGS_TOKEN.getText());
    }
    
    public static boolean isStartCondition(Token token) {
        return isStartCondition(token.getType());
    }
    
    public static boolean isStartCondition(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.IFDEF:
            case APTTokenTypes.IFNDEF:
            case APTTokenTypes.IF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isStartConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isStartOrSwitchConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
            case APT.Type.ELIF:
            case APT.Type.ELSE:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isEndCondition(Token token) {
        return isEndCondition(token.getType());
    }
    
    public static boolean isEndCondition(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.ELIF:
            case APTTokenTypes.ELSE:
            case APTTokenTypes.ENDIF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isConditionsBlockToken(Token token) {
        assert (token != null);
        return isConditionsBlockToken(token.getType());
    }
    
    public static boolean isConditionsBlockToken(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.IFDEF:
            case APTTokenTypes.IFNDEF:
            case APTTokenTypes.IF:
            case APTTokenTypes.ELIF:
            case APTTokenTypes.ELSE:
            case APTTokenTypes.ENDIF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isCommentToken(Token token) {
        assert (token != null);
        return isCommentToken(token.getType());
    }
    
    public static boolean isCommentToken(int ttype) {
        switch (ttype) {
            case APTTokenTypes.COMMENT:
            case APTTokenTypes.CPP_COMMENT:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isOpenBracket(Token token) {
        assert (token != null);
        return isOpenBracket(token.getType());
    }
    
    public static boolean isOpenBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.LCURLY:
            case APTTokenTypes.LPAREN:
            case APTTokenTypes.LSQUARE:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isCloseBracket(Token token) {
        assert (token != null);
        return isCloseBracket(token.getType());
    }
    
    public static boolean isCloseBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.RCURLY:
            case APTTokenTypes.RPAREN:
            case APTTokenTypes.RSQUARE:
                return true;
            default:
                return false;
        }
    }
    
    public static int getMatchBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.RCURLY:
                return APTTokenTypes.LCURLY;
            case APTTokenTypes.RPAREN:
                return APTTokenTypes.LPAREN;
            case APTTokenTypes.RSQUARE:
                return APTTokenTypes.LSQUARE;
            case APTTokenTypes.LCURLY:
                return APTTokenTypes.RCURLY;
            case APTTokenTypes.LPAREN:
                return APTTokenTypes.RPAREN;
            case APTTokenTypes.LSQUARE:
                return APTTokenTypes.RSQUARE;
            default:
                return APTUtils.EOF_TOKEN.EOF_TYPE;
        }
    }    
    
    public static boolean isEndDirectiveToken(int ttype) {
        switch(ttype) {
            case APTTokenTypes.END_PREPROC_DIRECTIVE:
            case APTTokenTypes.EOF:
                return true;
        }
        return false;
    }
    
    public static List<Token> toList(TokenStream ts) {
        List<Token> tokens = new ArrayList<Token>();
        try {
            Token token = ts.nextToken();
            while (!isEOF(token)) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                tokens.add(token);
                token = ts.nextToken();
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.INFO, "error on converting token stream to list", ex.getMessage()); // NOI18N
        }
        return tokens;
    }
    
    public static Object getTextKey(String text) {
        assert (text != null);
        assert (text.length() > 0);
        // now use text as is, but it will be faster to use textID
        return text;
    }
    
    public static String getTokenTextKey(Token token) {
        assert (token != null);
        // now use text, but it will be faster to use textID
        return token.getText();
    }
    
    public static APTToken createAPTToken(Token token, int ttype) {
        APTToken newToken = null;
        if (APTTraceFlags.USE_APT_TEST_TOKEN) {
            newToken = new APTTestToken(token, ttype);
        } else {
            newToken = new APTBaseToken(token, ttype);
        }
        return newToken;
    }
    
    public static APTToken createAPTToken(Token token) {
        return createAPTToken(token, token.getType());
    }
    
    public static APTToken createAPTToken() {
        APTToken newToken = null;
        if (APTTraceFlags.USE_APT_TEST_TOKEN) {
            newToken = new APTTestToken();
        } else {
            newToken = new APTBaseToken();
        }
        return newToken;
    }
    
    public static final APTToken VA_ARGS_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final APTToken EMPTY_ID_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final APTToken COMMA_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    static {
        VA_ARGS_TOKEN = createAPTToken();
        VA_ARGS_TOKEN.setType(APTTokenTypes.ID);
        VA_ARGS_TOKEN.setText("__VA_ARGS__"); // NOI18N
        
        EMPTY_ID_TOKEN = createAPTToken();
        EMPTY_ID_TOKEN.setType(APTTokenTypes.ID);
        EMPTY_ID_TOKEN.setText(""); // NOI18N        

        COMMA_TOKEN = createAPTToken();
        COMMA_TOKEN.setType(APTTokenTypes.COMMA);
        COMMA_TOKEN.setText(","); // NOI18N             
    }
    
    public static final APTToken EOF_TOKEN = new APTEOFToken();
    
    public static final TokenStream EMPTY_STREAM = new TokenStream() {
        public Token nextToken() throws TokenStreamException {
            return EOF_TOKEN;
        }
    };
    
    private static class APTEOFToken extends APTTokenAbstact {
        public APTEOFToken() {
        }
        
        public int getOffset() {
            throw new UnsupportedOperationException("getOffset must not be used"); // NOI18N
        }
        
        public void setOffset(int o) {
            throw new UnsupportedOperationException("setOffset must not be used"); // NOI18N
        }
        
        public int getEndOffset() {
            throw new UnsupportedOperationException("getEndOffset must not be used"); // NOI18N
        }
        
        public void setEndOffset(int o) {
            throw new UnsupportedOperationException("setEndOffset must not be used"); // NOI18N
        }
        
        public int getTextID() {
            throw new UnsupportedOperationException("getTextID must not be used"); // NOI18N
        }
        
        public void setTextID(int id) {
            throw new UnsupportedOperationException("setTextID must not be used"); // NOI18N
        }
        
        public int getEndColumn() {
            throw new UnsupportedOperationException("getEndColumn must not be used"); // NOI18N
        }
        
        public void setEndColumn(int c) {
            throw new UnsupportedOperationException("setEndColumn must not be used"); // NOI18N
        }
        
        public int getEndLine() {
            throw new UnsupportedOperationException("getEndLine must not be used"); // NOI18N
        }
        
        public void setEndLine(int l) {
            throw new UnsupportedOperationException("setEndLine must not be used"); // NOI18N
        }
        
        public int getType() {
            return APTTokenTypes.EOF;
        }
    }
}
