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

package org.netbeans.modules.cnd.apt.support;

import antlr.RecognitionException;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamSelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.impl.support.*;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * TokenStream responsible to expand all containing macros and as result
 * the return value of nextToken() need not be checked for macro substitution
 * @author Vladimir Voskresensky
 */
public class APTExpandedStream implements TokenStream, APTTokenStream {

    private final TokenStreamSelector selector = new TokenStreamSelector();
    
    /** callback to be used for macro substitutions */
    private final APTMacroCallback callback;
    
    /** 
     * state to indicate the phase of extracting params for macros 
     * in this state next token is not tried to be macro substituted
     */
    private boolean extractingMacroParams = false;
    
    // flag to specify that original stream is from preprocessor expression
    private final boolean expandPPExpression;
    
    public APTExpandedStream(TokenStream stream, APTMacroCallback callback, boolean expandPPExpression) {
        selector.select(stream);
        this.callback = callback;
        this.expandPPExpression = expandPPExpression;
        assert (!(callback instanceof APTSystemMacroMap)):"system macro map can't be used as callback"; // NOI18N
    }
    
    /**
     * Creates a new instance of APTExpandedStream
     */
    public APTExpandedStream(TokenStream stream, APTMacroCallback callback) {
        this(stream, callback, false);
    }

    /**
     * implementation of TokenStream interface
     */
    public APTToken nextToken() {
        for (;;) {
            APTToken token;
            try {
                token = (APTToken) selector.nextToken();
            } catch (TokenStreamException ex) {
                APTUtils.LOG.log(Level.SEVERE, null, ex);
                return APTUtils.EOF_TOKEN;
            }
            if (extractingMacroParams) {
                // extracting parameters doesn't need any activity
                // just return the next token
                return token;
            } else {
                // get token from selector and check for ID tokens
                // only ID tokens are candidates for macro expanding               
                boolean switchMacroExpanding = false;
                if (APTUtils.isID(token)) {
                    // check if ID needs macro expanding
                    // but prevent recursive re expanding
                    APTMacro macro = callback.getMacro(token);
                    if ((macro != null) && !callback.isExpanding(token)) {
                        try {
                            // start macro expanding
                            switchMacroExpanding = pushMacroExpanding(token, macro);
                        } catch (TokenStreamException ex) {
                            APTUtils.LOG.log(Level.SEVERE, null, ex);
                            switchMacroExpanding = false;
                        }
                    }
                } else if (APTUtils.isEOF(token)) {
                    // we got EOF on non empty selector => current stream should be poped
                    // it was end of macro expanding
                    switchMacroExpanding = continueOnEOF();
                }
                // return token if it was not start or end of macro expanding activity
                if (!switchMacroExpanding) {
                    return token;
                }
            }
        }
    }

    private boolean isExpandingPPExpression() {
        return expandPPExpression;
    }
    
    private boolean pushMacroExpanding(APTToken token, APTMacro macro) throws TokenStreamException {
        boolean res = true;
        try {
            APTTokenStream expanded = createMacroBodyWrapper(token, macro);
            // remember macro currently expanding
            res = callback.pushExpanding(token);    
            if (res) {
                // push wrapper into selector
                selector.push((TokenStream)expanded);
            }
        } catch (RecognitionException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on expanding " + token, ex); // NOI18N
            res = false;
        }    
        return res;
    }
    
    private boolean popMacroExpanding() {
        // macro was complitely expanded, 
        // notify callback and pop current stream from selector
        boolean res = true;
        callback.popExpanding();
        selector.pop();
        return res;
    }    
    
    protected APTTokenStream createMacroBodyWrapper(APTToken token, APTMacro macro) throws TokenStreamException, RecognitionException {
        // associated macro must be valid
        assert (macro != null) : "must be valid macro object"; // NOI18N
        assert (macro.getName() != null) : 
                "macro object must have valid name token"; // NOI18N
        assert (!callback.isExpanding(macro.getName())) : 
                "macro must not be under recursive expanding"; // NOI18N
        // use body's stream depending on kind of token
        // the out will have start offset for all tokens the same as original token
        APTTokenStream out;

        // clear info about RPAREN
        paramsRParen = null;
        
        if (!macro.isFunctionLike()) {
            // for object-like macro the body doesn't need any parameter expandings
            // use it as is in macro
            out = new APTCommentsFilter(macro.getBody());
            // in case of expanding stream in preprocessor directive expression
            // all #define'ed macro without body must be considered as having default macro body
            if (isExpandingPPExpression()) {
                if (APTUtils.isEOF(out.nextToken())) {
                    // no body => use default
                    out = new ListBasedTokenStream(APTUtils.DEF_MACRO_BODY);
                } else {
                    // has body => restore original eaten by the nextToken call above
                    out = new APTCommentsFilter(macro.getBody());
                }
            }
        } else {
            // create wrapper for function-like macro:
	    
            APTToken next;
            boolean cont;
            do {
                cont = false;
                // the first token must be LPAREN
                // but skip all comments
                do {
                    next = (APTToken) selector.nextToken();
                } while (APTUtils.isCommentToken(next));
                cont = APTUtils.isEOF(next) && continueOnEOF();
            } while (cont);
	    if (next.getType() == APTTokenTypes.LPAREN) {
		// - extract macro parameters 
		List<List<APTToken>> params = extractParams(macro, token, next);
		// - subsitute all parameters in macro body
		List<APTToken> substParamsList = subsituteParams(macro, params, callback, isExpandingPPExpression());
		// - put result list in TokenStream wrapper
		out = new ListBasedTokenStream(substParamsList);
	    }
	    else {
		// if function-like macro us used without parenthesis, 
		// it shouldn't be expanded
		List<APTToken> l = new ArrayList<APTToken>(2);
		l.add(token);
		l.add(next);
		out = new ListBasedTokenStream(l);
	    }
        }  
        APTUtils.LOG.log(Level.INFO, 
                        "token {0} \n was expanded by macro substitution to \n {1}", // NOI18N
                        new Object[] { token, out }); 
        
        return out;
    }

    private boolean continueOnEOF() {
        boolean cont = false;
        if (!selector.isEmpty()) {
            // we got EOF on non empty selector => current stream should be poped
            // it was end of macro expanding, but continue on next stream
            cont = popMacroExpanding();
        }
        return cont;
    }
    
    private APTToken paramsRParen = null;
    
    protected final APTToken getLastExtractedParamRPAREN() {
        return paramsRParen;
    }
    
    private List<List<APTToken>> extractParams(APTMacro macro, APTToken token, APTToken next) throws TokenStreamException, RecognitionException {
        // set special state to prevent macro expanding of parameters
        assert extractingMacroParams == false : "already extracting params";
        extractingMacroParams = true;
        // Array of parameters. Each parameter is list of tokens
        List<List<APTToken>> params = new ArrayList<List<APTToken>>();
        try {
            if (next.getType() != APTTokenTypes.LPAREN) {
                throw new RecognitionException("Error on expanding " + token + "\n by macro " + macro + // NOI18N
                                                "\n Expecting LPAREN, found " + next); // NOI18N
            }  
            // use balanced parens for correct detecting of ended parameter 
            int paren = 0;
            // Each parameter is list of tokens
            List<APTToken> param = new ArrayList<APTToken>();
            for (next = nextToken(); !APTUtils.isEOF(next) || continueOnEOF(); next = nextToken()) {
                int type = next.getType();
                if (type == APTTokenTypes.LPAREN) {
                    // add this "(" to parameter
                    add2Param(param, next);
                    paren++;
                } else if (type == APTTokenTypes.RPAREN) {
                    if (paren == 0) {
                        // we skipped all params
                        // add last param
                        params.add(param);
                        param = null;
                        // remember the position of the RPAREN
                        paramsRParen = next;
                        break;
                    } else {
                        // add this ")" to parameter
                        add2Param(param, next);
                        paren--;
                        if (paren < 0) {                              
                            throw new RecognitionException("Error on expanding " + token + "\n by macro " + macro + // NOI18N
                                    "\n Unbalanced RPAREN " + next); // NOI18N
                        }
                    }
                } else if (type == APTTokenTypes.COMMA && paren == 0) {
                    // params delimeter
                    // add new param
                    params.add(param);
                    param = new ArrayList<APTToken>();
                } else {
                    // add token to parameter
                    add2Param(param, next);
                }
            }         
            // check for error
            if (APTUtils.isEOF(next)) {
                APTUtils.LOG.log(Level.SEVERE, "error expanding macro " + token + " : unterminated arguments list");// NOI18N
            }
        } finally {
            extractingMacroParams = false;              
        }        
        return params;
    }
    
    private void add2Param( List<APTToken> param, APTToken next) {
        // any non comment token is valid in parameters
        if (!APTUtils.isCommentToken(next)) {
            param.add(next);
        }
    }         
    
    private static final int BODY_STREAM = 0;
    private static final int STRINGIZE_PARAM = 1;
    private static final int CONCATENATE = 2; 
    // threashold to limit the size of expanded macro parameters
    // boost uses delay.c test from boost_1_33_1/libs/preprocessor/doc/examples/delay.c
    // to slow down everything using preprocessor only
    // gcc consumes 2.5G and fails, 
    // we are trying to prevent such experiments, especially in IDE
    private static final long MACRO_EXPANDING_THREASHOLD = 16*1024;
    private static List<APTToken> subsituteParams(APTMacro macro, List<List<APTToken>> params, APTMacroCallback callback, boolean expandPPExpression) throws TokenStreamException {
        final Map<CharSequence/*getTokenTextKey(token)*/, List<APTToken>> paramsMap = createParamsMap(macro, params);
        final List<APTToken> expanded = new LinkedList<APTToken>();
        final APTTokenStream body = new APTCommentsFilter(macro.getBody());
        int state = BODY_STREAM;
        APTToken token = null;
        APTToken laToken = body.nextToken();
        APTToken leftConcatToken = null;
        do {
            switch (state) {
                case BODY_STREAM:
                {
                    token = laToken;
                    laToken = body.nextToken();  
                    // the most important is ## concatenation
                    // it available only by lookup
                    switch (laToken.getType()) {
                        case APTTokenTypes.DBL_SHARP:
                        {
                            leftConcatToken = token;
                            state = CONCATENATE;
                            token = null;
                            break;
                        }                        
                        default:
                        switch (token.getType()) {
                            case APTTokenTypes.SHARP:
                            {
                                // stringize next token, it must be param!      
                                state = STRINGIZE_PARAM;
                                token = null;
                                break;                            
                            }
                            case APTTokenTypes.ID:
                            {
                                // may be it is parameter of macro to substitute with input parameter value  
                                List<APTToken> paramValue = paramsMap.get(token.getTextID());
                                if (paramValue != null) {
                                    // found param, so expand it and skip current token
                                    List<APTToken> expandedValue = expandParamValue(paramValue, callback, expandPPExpression);
                                    List<APTToken> expandedValueWrapper = new ArrayList<APTToken>();
                                    for (APTToken t : expandedValue) {
                                        expandedValueWrapper.add(new APTMacroParamExpansion(t, token));
                                    }
                                    expandedValue = expandedValueWrapper;
                                    if (expandedValue.size() > MACRO_EXPANDING_THREASHOLD) {
                                        if (DebugUtils.STANDALONE) {
                                            System.err.printf(
                                                    "parameter '%s' was empty substituted due to very long output value when expanding macros:\n %s\n", // NOI18N
                                                    token.getText(), macro.getName());
                                        } else {
                                            APTUtils.LOG.log(Level.WARNING,
                                                    "parameter '{0}' was empty substituted due to very long output value when expanding macros:\n {1}\n", // NOI18N
                                                    new Object[] {token.getText(), macro.getName()});
                                        }
                                        return Collections.<APTToken>emptyList();
                                    }
                                    token = null;
                                    expanded.addAll(expandedValue);
                                }
                                break;
                            } 
                        }                       
                    }
                    break;
                }
                case CONCATENATE: // concatenation of left and right tokens around ##
                {
                    token = null;
                    assert (laToken.getType() == APTTokenTypes.DBL_SHARP);
                    assert (leftConcatToken != null);
                    List<APTToken> rightConcatTokens = new ArrayList<APTToken>();
                    rightConcatTokens.add(body.nextToken());
                    laToken = body.nextToken();
                    if (isImplicitConcat(rightConcatTokens.get(0), laToken)) {
                        // Fix for IZ#149225: incorrect concatenation with token that starts with digit
                        rightConcatTokens.add(laToken);
                        laToken = body.nextToken();
                    }
                    List<APTToken> concatList = createConcatenation(leftConcatToken, rightConcatTokens, paramsMap);
                    switch (laToken.getType()) {
                        case APTTokenTypes.DBL_SHARP:
                        {
                            // on more ##, like 
                            // #define var(name,ind) m_##name##ind;
                            // remember right most of concatenated tokens to use on next iteration
                            int lastInd = concatList.size() - 1;
                            leftConcatToken = (lastInd < 0) ? APTUtils.EMPTY_ID_TOKEN : concatList.remove(lastInd);
                            if (concatList.size() > 0) {
                                // add remains left tokens to output
                                expanded.addAll(concatList);
                            }
                            state = CONCATENATE;
                            break;
                        }
                        default:
                        {
                            leftConcatToken = null;
                            expanded.addAll(concatList);
                            state = BODY_STREAM;
                        }
                    }
                    break;
                }
                case STRINGIZE_PARAM: // stringizing token after #
                {  
                    token = null;                    
                    // stringize next token, it must be param
                    // unless macro is incomplete
                    if (laToken != null && laToken.getType() == APTTokenTypes.ID) {
                        APTToken stringized = stringizeParam(paramsMap.get(laToken.getTextID()));
                        laToken = body.nextToken();
                        switch (laToken.getType()) {
                            case APTTokenTypes.DBL_SHARP:
                            {
                                leftConcatToken = stringized;
                                state = CONCATENATE;
                                break;
                            }
                            default:
                            {
                                token = stringized;
                                state = BODY_STREAM;
                            }
                        }
                    } else {
                        state = BODY_STREAM;
                    }
                    break;
                }
            }
            if (token != null) {
                if (APTUtils.isEOF(token)) {
                    break;
                } else {
                    expanded.add(token);
                    token = null;
                }
            }
        } while (token == null);        
        return expanded;
    }

    private static boolean isImplicitConcat(APTToken left, APTToken right) {
        return APTUtils.isInt(left) && APTUtils.isID(right) && APTUtils.areAdjacent(left, right);
    }

    private static Map<CharSequence, List<APTToken>> createParamsMap(APTMacro macro, List<List<APTToken>> params) {
        Map<CharSequence, List<APTToken>> map = new HashMap<CharSequence, List<APTToken>>();
        Collection<APTToken> macroParams = macro.getParams();
        int numInList = params.size();
        int i=0;
        APTToken lastMacroParam = null;
        for (APTToken macroParam : macroParams) {
            map.put(macroParam.getTextID(), i < numInList ? params.get(i) : Collections.<APTToken>emptyList());
            i++;
            lastMacroParam = macroParam;
        }
        // TODO: need to support ELLIPSIS for IZ#83949
        // if remains values and last param of macro is VA_ARG => 
        // add all remains to the last value separating by comma as it was in macro call
        if (i < numInList && APTUtils.isVaArgsToken(lastMacroParam)) {
            List<APTToken> vaArgsVal = map.get(lastMacroParam.getTextID());
            for (; i < numInList; i++) {
                vaArgsVal.add(APTUtils.COMMA_TOKEN);
                vaArgsVal.addAll(params.get(i));
            }
        }
        return map;
    }    

    private static List<APTToken> createConcatenation(APTToken tokenLeft, List<APTToken> tokensRight, final Map<CharSequence/*getTokenTextKey(token)*/, List<APTToken>> paramsMap) {
        //TODO: finish it, use lexer
        List<APTToken> valLeft = paramsMap.get(tokenLeft.getTextID());
        String leftText;
        if (valLeft != null) {
            leftText = toText(valLeft, false);
        } else {
            leftText = tokenLeft.getText();
        }
        StringBuilder tokensRightMerged = new StringBuilder();
        for (APTToken token : tokensRight) {
            tokensRightMerged.append(token.getTextID());
        }
        List<APTToken> valRight = paramsMap.get(CharSequenceKey.create(tokensRightMerged));
        String rightText;
        if (valRight != null) {
            rightText = toText(valRight, false);
        } else {
            rightText = tokensRightMerged.toString();
        }
        // IZ#149505: special handling of __VA_ARGS__ with preceding comma
        if (tokenLeft.getType() == APTTokenTypes.COMMA && rightText.length() == 0 && 
            APTUtils.isVaArgsToken(tokensRight.get(0))) {
            // when __VA_ARGS__ is empty expanded => 
            // need to eat comma as well and should return no tokens
            return new ArrayList<APTToken>();
        }
        String text = leftText + rightText;
        TokenStream ts = APTTokenStreamBuilder.buildTokenStream(text);
        List<APTToken> tokens = APTUtils.toList(ts);
        return tokens;
    }

    private static APTToken stringizeParam(List<APTToken> param) {
        //TODO: toText should consider whitespaces correctly
        assert (param != null);
        APTToken token = APTUtils.createAPTToken();
        token.setType(APTTokenTypes.STRING_LITERAL);
        token.setText(toText(param, true));
        return token;
    }
    
    private static String toText(List<APTToken> tokens, boolean stringize) {
        StringBuilder out = new StringBuilder();
        if (stringize) {
            out.append('"'); // NOI18N
        }
        for (int i = 0; i < tokens.size(); ++i) {
            APTToken token = tokens.get(i);
            if (stringize) {
                out.append(escape(token.getTextID()));
            } else {
                out.append(token.getTextID());
                if (i + 1 < tokens.size() && !APTUtils.areAdjacent(token, tokens.get(i + 1))) {
                    out.append(' '); // NOI18N
                }
            }
        }
        if (stringize) {
            out.append('"'); // NOI18N
        }
        return out.toString();
    }
    
    private static CharSequence escape(CharSequence cs) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cs.length(); ++i) {
            char c = cs.charAt(i);
            if (c == '"' || c == '\\') {
                out.append('\\');
            }
            out.append(c);
        }
        return out;
    }

    private static List<APTToken> expandParamValue(List<APTToken> paramValue, APTMacroCallback callback, boolean expandPPExpression) {
        TokenStream valueStream = new ListBasedTokenStream(paramValue);
        TokenStream expanedValue = new APTExpandedStream(valueStream, callback, expandPPExpression);
        List<APTToken> out = APTUtils.toList(expanedValue);
        return out;
    }
}
