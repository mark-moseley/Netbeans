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

package org.netbeans.modules.php.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
%%

%public
%class PHP5ColoringLexer
%type PHPTokenId
%function nextToken
%unicode
%caseless
%char

%state ST_PHP_IN_SCRIPTING
%state ST_PHP_DOUBLE_QUOTES
%state ST_PHP_BACKQUOTE
%state ST_PHP_QUOTES_AFTER_VARIABLE
%state ST_PHP_HEREDOC
%state ST_PHP_START_HEREDOC
%state ST_PHP_END_HEREDOC
%state ST_PHP_NOWDOC
%state ST_PHP_START_NOWDOC
%state ST_PHP_END_NOWDOC
%state ST_PHP_LOOKING_FOR_PROPERTY
%state ST_PHP_VAR_OFFSET
%state ST_PHP_COMMENT
%state ST_PHP_DOC_COMMENT
%state ST_PHP_LINE_COMMENT
%state ST_PHP_HIGHLIGHTING_ERROR

%eofval{
       if(input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as error token
            return PHPTokenId.UNKNOWN_TOKEN;
        } else {
            return null;
        }
%eofval}

%{

    protected String heredoc = null;
    protected int heredoc_len = 0;
    private boolean asp_tags = false;
    private StateStack stack = new StateStack();

    private boolean short_tags_allowed = true;

    private LexerInput input;
    
    /*public PhpLexer5(int state){
        initialize(state);
    }*/
    /*public void reset(char array[], int offset, int length) {
        this.zzBuffer = array;
        this.zzCurrentPos = offset;
        this.zzMarkedPos = offset;
        this.zzPushbackPos = offset;
        this.yychar = offset;
        this.zzEndRead = offset + length;
        this.zzStartRead = offset;
        this.zzAtEOF = zzCurrentPos >= zzEndRead;
        this.firstPos = offset;
    }

    

    public void reset(java.io.Reader  reader, char[] buffer, int[] parameters){
    	this.zzReader = reader;
    	this.zzBuffer = buffer;
    	this.zzMarkedPos = parameters[0];
    	this.zzPushbackPos = parameters[1];
    	this.zzCurrentPos = parameters[2];
    	this.zzStartRead = parameters[3];
    	this.zzEndRead = parameters[4];
    	this.yyline = parameters[5];  
    	initialize(parameters[6]);
    }
    */
        public PHP5ColoringLexer(LexerRestartInfo info, boolean asp_tags) {
            this.input = info.input();
            this.asp_tags = asp_tags;
            
            if(info.state() != null) {
                //reset state
                setState((LexerState)info.state());
            } else {
                //initial state
                zzState = zzLexicalState = YYINITIAL;
                stack.clear();
            }
            
        }

        public static final class LexerState  {
            final StateStack stack;
            /** the current state of the DFA */
            final int zzState;
            /** the current lexical state */
            final int zzLexicalState;
            /** remember the heredoc */
            final String heredoc;
            /** and the lenght of */
            final int heredoc_len;
            
            LexerState (StateStack stack, int zzState, int zzLexicalState, String heredoc, int heredoc_len) {
                this.stack = stack;
                this.zzState = zzState;
                this.zzLexicalState = zzLexicalState;
                this.heredoc = heredoc;
                this.heredoc_len = heredoc_len;
            }
            
            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
			return true;
		}

		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
                
                LexerState state = (LexerState) obj;
                return (this.stack.equals(state.stack) 
                    && (this.zzState == state.zzState) 
                    && (this.zzLexicalState == state.zzLexicalState)
                    && (this.heredoc_len == state.heredoc_len)
                    && ((this.heredoc == null && state.heredoc == null) || (this.heredoc != null && state.heredoc != null && this.heredoc.equals(state.heredoc))));
            }
         
            @Override
            public int hashCode() {
                int hash = 11;
                hash = 31 * hash + this.zzState;
                hash = 31 * hash + this.zzLexicalState;
                hash = 31 * hash + this.stack.hashCode();
                hash = 31 * hash + this.heredoc_len;
                hash = 31 * hash + this.heredoc.hashCode();
                return hash;
            }
        }
        
        public LexerState getState() {
            return new LexerState(stack.createClone(), zzState, zzLexicalState, heredoc, heredoc_len);
        }
        
        public void setState(LexerState state) {
            this.stack.copyFrom(state.stack);
            this.zzState = state.zzState;
            this.zzLexicalState = state.zzLexicalState;
            this.heredoc = state.heredoc;
            this.heredoc_len = state.heredoc_len;
        }

     protected boolean isHeredocState(int state){
    	    	return state == ST_PHP_HEREDOC || state == ST_PHP_START_HEREDOC || state == ST_PHP_END_HEREDOC;
    }
    
    public int[] getParamenters(){
    	return new int[]{zzMarkedPos, zzPushbackPos, zzCurrentPos, zzStartRead, zzEndRead, yyline, zzLexicalState};
    }

    protected int getZZLexicalState() {
        return zzLexicalState;
    }

    protected int getZZMarkedPos() {
        return zzMarkedPos;
    }

    protected int getZZEndRead() {
        return zzEndRead;
    }

    public char[] getZZBuffer() {
        return zzBuffer;
    }
    
    protected int getZZStartRead() {
    	return this.zzStartRead;
    }

    protected int getZZPushBackPosition() {
    	return this.zzPushbackPos;
    }

        protected void pushBack(int i) {
		yypushback(i);
	}

        protected void popState() {
		yybegin(stack.popStack());
	}

	protected void pushState(final int state) {
		stack.pushStack(getZZLexicalState());
		yybegin(state);
	}

    
 // End user code

%}

LNUM=[0-9]+
DNUM=([0-9]*[\.][0-9]+)|([0-9]+[\.][0-9]*)
EXPONENT_DNUM=(({LNUM}|{DNUM})[eE][+-]?{LNUM})
HNUM="0x"[0-9a-fA-F]+
//LABEL=[a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*
LABEL=[[:letter:]_\x7f-\xff][[:letter:][:digit:]_\x7f-\xff]*
WHITESPACE=[ \n\r\t]+
TABS_AND_SPACES=[ \t]*
TOKENS=[:,.\[\]()|\^&+-//*=%!~$<>?@]
CLOSE_EXPRESSION=[;]
ANY_CHAR=(.|[\n])
NEWLINE=("\r"|"\n"|"\r\n")
DOUBLE_QUOTES_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\"\\{]|("\\"{ANY_CHAR})))
BACKQUOTE_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$`\\{]|("\\"{ANY_CHAR})))

HEREDOC_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\n\r\\{]|("\\"[^\n\r])))
HEREDOC_NEWLINE=((({LABEL}";"?((("{"+|"$"+)"\\"?)|"\\"))|(("{"*|"$"*)"\\"?)){NEWLINE}) 
HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR=(("{"+[^$\n\r\\{])|("{"*"\\"[^\n\r])|{HEREDOC_LITERAL_DOLLAR})
HEREDOC_NON_LABEL=([^a-zA-Z_\x7f-\xff$\n\r\\{]|{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})
HEREDOC_LABEL_NO_NEWLINE=({LABEL}([^a-zA-Z0-9_\x7f-\xff;$\n\r\\{]|(";"[^$\n\r\\{])|(";"?{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})))

DOUBLE_QUOTES_CHARS=("{"*([^$\"\\{]|("\\"{ANY_CHAR}))|{DOUBLE_QUOTES_LITERAL_DOLLAR})
BACKQUOTE_CHARS=("{"*([^$`\\{]|("\\"{ANY_CHAR}))|{BACKQUOTE_LITERAL_DOLLAR})

HEREDOC_CHARS=("{"*([^$\n\r\\{]|("\\"[^\n\r]))|{HEREDOC_LITERAL_DOLLAR}|({HEREDOC_NEWLINE}+({HEREDOC_NON_LABEL}|{HEREDOC_LABEL_NO_NEWLINE})))
NOWDOC_CHARS=({NEWLINE}*(([^a-zA-Z_\x7f-\xff\n\r][^\n\r]*)|({LABEL}[^a-zA-Z0-9_\x7f-\xff;\n\r][^\n\r]*)|({LABEL}[;][^\n\r]+)))
PHP_OPERATOR=       "=>"|"++"|"--"|"==="|"!=="|"=="|"!="|"<>"|"<="|">="|"+="|"-="|"*="|"/="|".="|"%="|"<<="|">>="|"&="|"|="|"^="|"||"|"&&"|"OR"|"AND"|"XOR"|"<<"|">>"






%%

<YYINITIAL>(([^<]|"<"[^?%(script)<])+)|"<script"|"<" {
    return PHPTokenId.T_INLINE_HTML;
}

<YYINITIAL>"<script"{WHITESPACE}+"language"{WHITESPACE}*"="{WHITESPACE}*("php"|"\"php\""|"\'php\'"){WHITESPACE}*">" {
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.T_INLINE_HTML;
}

<YYINITIAL>"<?" {
    if (short_tags_allowed ) {
        //yybegin(ST_PHP_IN_SCRIPTING);
        pushState(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_OPENTAG;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<%="|"<?=" {
    String text = yytext();
    if ((text.charAt(1)=='%' && asp_tags)
        || (text.charAt(1)=='?' && short_tags_allowed)) {
        //yybegin(ST_PHP_IN_SCRIPTING);
        pushState(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.T_OPEN_TAG_WITH_ECHO;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<%" {
    if (asp_tags) {
        //yybegin(ST_PHP_IN_SCRIPTING);
        pushState(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_OPENTAG;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<?php" {
    pushState(ST_PHP_IN_SCRIPTING);
    //yybegin(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_OPENTAG;
    //return createSymbol(ASTSymbol.T_OPEN_TAG);
}


/***********************************************************************************************
**************************************** P  H  P ***********************************************
***********************************************************************************************/

<ST_PHP_IN_SCRIPTING> "exit" {
    return PHPTokenId.PHP_EXIT;
}

<ST_PHP_IN_SCRIPTING>"die" {
    return PHPTokenId.PHP_DIE;
}

<ST_PHP_IN_SCRIPTING>"function" {
    return PHPTokenId.PHP_FUNCTION;
}

<ST_PHP_IN_SCRIPTING>"const" {
    return PHPTokenId.PHP_CONST;
}

<ST_PHP_IN_SCRIPTING>"return" {
    return PHPTokenId.PHP_RETURN;
}

<ST_PHP_IN_SCRIPTING>"try" {
    return PHPTokenId.PHP_TRY;
}

<ST_PHP_IN_SCRIPTING>"catch" {
    return PHPTokenId.PHP_CATCH;
}

<ST_PHP_IN_SCRIPTING>"throw" {
    return PHPTokenId.PHP_THROW;
}

<ST_PHP_IN_SCRIPTING>"if" {
    return PHPTokenId.PHP_IF;
}

<ST_PHP_IN_SCRIPTING>"elseif" {
    return PHPTokenId.PHP_ELSEIF;
}

<ST_PHP_IN_SCRIPTING>"endif" {
    return PHPTokenId.PHP_ENDIF;
}

<ST_PHP_IN_SCRIPTING>"else" {
    return PHPTokenId.PHP_ELSE;
}

<ST_PHP_IN_SCRIPTING>"while" {
    return PHPTokenId.PHP_WHILE;
}

<ST_PHP_IN_SCRIPTING>"endwhile" {
    return PHPTokenId.PHP_ENDWHILE;
}

<ST_PHP_IN_SCRIPTING>"do" {
    return PHPTokenId.PHP_DO;
}

<ST_PHP_IN_SCRIPTING>"for" {
    return PHPTokenId.PHP_FOR;
}

<ST_PHP_IN_SCRIPTING>"endfor" {
    return PHPTokenId.PHP_ENDFOR;
}

<ST_PHP_IN_SCRIPTING>"foreach" {
    return PHPTokenId.PHP_FOREACH;
}

<ST_PHP_IN_SCRIPTING>"endforeach" {
    return PHPTokenId.PHP_ENDFOREACH;
}

<ST_PHP_IN_SCRIPTING>"declare" {
    return PHPTokenId.PHP_DECLARE;
}

<ST_PHP_IN_SCRIPTING>"enddeclare" {
    return PHPTokenId.PHP_ENDDECLARE;
}

<ST_PHP_IN_SCRIPTING>"instanceof" {
    return PHPTokenId.PHP_INSTANCEOF;
}

<ST_PHP_IN_SCRIPTING>"as" {
    return PHPTokenId.PHP_AS;
}

<ST_PHP_IN_SCRIPTING>"switch" {
    return PHPTokenId.PHP_SWITCH;
}

<ST_PHP_IN_SCRIPTING>"endswitch" {
    return PHPTokenId.PHP_ENDSWITCH;
}

<ST_PHP_IN_SCRIPTING>"case" {
    return PHPTokenId.PHP_CASE;
}

<ST_PHP_IN_SCRIPTING>"default" {
    return PHPTokenId.PHP_DEFAULT;
}

<ST_PHP_IN_SCRIPTING>"break" {
    return PHPTokenId.PHP_BREAK;
}

<ST_PHP_IN_SCRIPTING>"continue" {
    return PHPTokenId.PHP_CONTINUE;
}

<ST_PHP_IN_SCRIPTING>"echo" {
    return PHPTokenId.PHP_ECHO;
}

<ST_PHP_IN_SCRIPTING>"print" {
    return PHPTokenId.PHP_PRINT;
}

<ST_PHP_IN_SCRIPTING>"class" {
    return PHPTokenId.PHP_CLASS;
}

<ST_PHP_IN_SCRIPTING>"interface" {
    return PHPTokenId.PHP_INTERFACE;
}

<ST_PHP_IN_SCRIPTING>"extends" {
    return PHPTokenId.PHP_EXTENDS;
}

<ST_PHP_IN_SCRIPTING>"implements" {
    return PHPTokenId.PHP_IMPLEMENTS;
}

<ST_PHP_IN_SCRIPTING>"self" {
    return PHPTokenId.PHP_SELF;
}

<ST_PHP_IN_SCRIPTING>"->" {
    pushState(ST_PHP_LOOKING_FOR_PROPERTY);
    return PHPTokenId.PHP_OBJECT_OPERATOR;
}

<ST_PHP_QUOTES_AFTER_VARIABLE> {
    "->" {
    popState();
    pushState(ST_PHP_LOOKING_FOR_PROPERTY);
    return PHPTokenId.PHP_OBJECT_OPERATOR;
    }
    {ANY_CHAR} {
        yypushback(1);
        popState();
    }
}

<ST_PHP_LOOKING_FOR_PROPERTY>"->" {
	return PHPTokenId.PHP_OBJECT_OPERATOR;
}

<ST_PHP_LOOKING_FOR_PROPERTY>{LABEL} {
    popState();
    return PHPTokenId.PHP_STRING;
}

<ST_PHP_LOOKING_FOR_PROPERTY>{ANY_CHAR} {
    yypushback(1);
    popState();
}

<ST_PHP_IN_SCRIPTING>"::" {
    return PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM;
}

<ST_PHP_IN_SCRIPTING>"new" {
    return PHPTokenId.PHP_NEW;
}

<ST_PHP_IN_SCRIPTING>"clone" {
    return PHPTokenId.PHP_CLONE;
}

<ST_PHP_IN_SCRIPTING>"var" {
    return PHPTokenId.PHP_VAR;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("int"|"integer"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("real"|"double"|"float"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"string"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"binary"{TABS_AND_SPACES}")" {
	return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"array"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"object"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("bool"|"boolean"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("unset"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"eval" {
    return PHPTokenId.PHP_EVAL;
}

<ST_PHP_IN_SCRIPTING>"include" {
    return PHPTokenId.PHP_INCLUDE;
}

<ST_PHP_IN_SCRIPTING>"include_once" {
    return PHPTokenId.PHP_INCLUDE_ONCE;
}

<ST_PHP_IN_SCRIPTING>"require" {
    return PHPTokenId.PHP_REQUIRE;
}

<ST_PHP_IN_SCRIPTING>"require_once" {
    return PHPTokenId.PHP_REQUIRE_ONCE;
}

<ST_PHP_IN_SCRIPTING>"use" {
    return PHPTokenId.PHP_USE;
}

<ST_PHP_IN_SCRIPTING>"global" {
    return PHPTokenId.PHP_GLOBAL;
}

<ST_PHP_IN_SCRIPTING>"isset" {
    return PHPTokenId.PHP_ISSET;
}

<ST_PHP_IN_SCRIPTING>"empty" {
    return PHPTokenId.PHP_EMPTY;
}

<ST_PHP_IN_SCRIPTING>"__halt_compiler" {
	return PHPTokenId.PHP_HALT_COMPILER;
}

<ST_PHP_IN_SCRIPTING>"static" {
    return PHPTokenId.PHP_STATIC;
}

<ST_PHP_IN_SCRIPTING>"abstract" {
    return PHPTokenId.PHP_ABSTRACT;
}

<ST_PHP_IN_SCRIPTING>"final" {
    return PHPTokenId.PHP_FINAL;
}

<ST_PHP_IN_SCRIPTING>"private" {
    return PHPTokenId.PHP_PRIVATE;
}

<ST_PHP_IN_SCRIPTING>"protected" {
    return PHPTokenId.PHP_PROTECTED;
}

<ST_PHP_IN_SCRIPTING>"public" {
    return PHPTokenId.PHP_PUBLIC;
}

<ST_PHP_IN_SCRIPTING>"unset" {
    return PHPTokenId.PHP_UNSET;
}

<ST_PHP_IN_SCRIPTING>"list" {
    return PHPTokenId.PHP_LIST;
}

<ST_PHP_IN_SCRIPTING>"array" {
    return PHPTokenId.PHP_ARRAY;
}

<ST_PHP_IN_SCRIPTING>"parent" {
    return PHPTokenId.PHP_PARENT;
}

<ST_PHP_IN_SCRIPTING>"from" {
    return PHPTokenId.PHP_FROM;
}

<ST_PHP_IN_SCRIPTING>"true" {
    return PHPTokenId.PHP_TRUE;
}

<ST_PHP_IN_SCRIPTING>"null" {
    return PHPTokenId.PHP_NULL;
}

<ST_PHP_IN_SCRIPTING>"false" {
    return PHPTokenId.PHP_FALSE;
}

<ST_PHP_IN_SCRIPTING>{PHP_OPERATOR} {
    return PHPTokenId.PHP_OPERATOR;
}

<ST_PHP_IN_SCRIPTING>{TOKENS} {
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_IN_SCRIPTING>{CLOSE_EXPRESSION} {
    return PHPTokenId.PHP_SEMICOLON;
}

<ST_PHP_IN_SCRIPTING>"{" {
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_CURLY_OPEN;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC>"${" {
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_IN_SCRIPTING>"}" {
             //  if (!stack.isEmpty()) {
            
            //we are pushing state when we enter the PHP code,
            //so we need to ensure we do not pop the top most state
            if(stack.size() > 1) {
                popState();
    }
    return  PHPTokenId.PHP_CURLY_CLOSE;
}

<ST_PHP_IN_SCRIPTING>{LNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>{HNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_VAR_OFFSET>0|([1-9][0-9]*) {
	return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_VAR_OFFSET>{LNUM}|{HNUM} {
	return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>{DNUM}|{EXPONENT_DNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>"__CLASS__" {
    return PHPTokenId.PHP__CLASS__;
}

<ST_PHP_IN_SCRIPTING>"__FUNCTION__" {
    return PHPTokenId.PHP__FUNCTION__;
}

<ST_PHP_IN_SCRIPTING>"__METHOD__" {
    return PHPTokenId.PHP__METHOD__;
}

<ST_PHP_IN_SCRIPTING>"__LINE__" {
    return PHPTokenId.PHP__LINE__;
}

<ST_PHP_IN_SCRIPTING>"__FILE__" {
    return PHPTokenId.PHP__FILE__;
}

<ST_PHP_IN_SCRIPTING>"$"{LABEL} {
    return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_VAR_OFFSET>"$"{LABEL} {
    pushState(ST_PHP_QUOTES_AFTER_VARIABLE);
    return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_HEREDOC,ST_PHP_BACKQUOTE>"$"{LABEL}"[" {
	yypushback(1);
	pushState(ST_PHP_VAR_OFFSET);
	return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_VAR_OFFSET>"]" {
	popState();
	return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_VAR_OFFSET>"[" { 
	return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_VAR_OFFSET>{TOKENS}|[;{}\"`] {//the difference from the original rules comes from the fact that we took ';' out out of tokens 
	return  PHPTokenId.UNKNOWN_TOKEN;
}

<ST_PHP_VAR_OFFSET>[ \n\r\t\\'#] {
	yypushback(1);
	popState();
        if (yylength() > 0)
            return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_IN_SCRIPTING,ST_PHP_VAR_OFFSET>{LABEL} {
    return  PHPTokenId.PHP_STRING;
}

<ST_PHP_IN_SCRIPTING>{WHITESPACE} {
    return  PHPTokenId.WHITESPACE;
}

<ST_PHP_IN_SCRIPTING>([#]|"//") {
    pushState(ST_PHP_LINE_COMMENT);
    return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>"?"|"%"|">" {
    return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>[^\n\r?%>]*{ANY_CHAR} {
	String yytext = yytext();
	switch (yytext.charAt(yytext.length() - 1)) {
		case '?':
		case '%':
		case '>':
			yypushback(1);
			break;
		default:
			popState();
	}
	 return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>{NEWLINE} {
    popState();
    return PHPTokenId.PHP_LINE_COMMENT;
}


<ST_PHP_IN_SCRIPTING>"/**"{WHITESPACE} {
    pushState(ST_PHP_DOC_COMMENT);
    yypushback(yylength()-3);
    return PHPTokenId.PHPDOC_COMMENT_START;
}

<ST_PHP_DOC_COMMENT>"*/" {
    popState();
    return PHPTokenId.PHPDOC_COMMENT_END;
}

<ST_PHP_DOC_COMMENT>~"*/" {
        yypushback(2); // go back to mark end of comment in the next token
        return PHPTokenId.PHPDOC_COMMENT;
}

<ST_PHP_DOC_COMMENT> <<EOF>> {
              if (input.readLength() > 0) {
                    input.backup(1);  // backup eof
                    return PHPTokenId.PHPDOC_COMMENT;
                }
                else {
                    return null;
                }
}

<ST_PHP_IN_SCRIPTING>"/*" {
    pushState(ST_PHP_COMMENT);
    return PHPTokenId.PHP_COMMENT_START;
}

<ST_PHP_COMMENT>"*/" {
    popState();
    return PHPTokenId.PHP_COMMENT_END;
}

<ST_PHP_COMMENT>~"*/" {
    yypushback(2);
    return PHPTokenId.PHP_COMMENT;
}

<ST_PHP_COMMENT> <<EOF>> {
              if (input.readLength() > 0) {
                input.backup(1);  // backup eof
                return PHPTokenId.PHP_COMMENT;
              }
              else {
                  return null;
              }
}

<ST_PHP_IN_SCRIPTING,ST_PHP_LINE_COMMENT>"?>"{WHITESPACE}? {
        //popState();
        yybegin(YYINITIAL);
        stack.clear();
	return PHPTokenId.PHP_CLOSETAG;
}

<ST_PHP_IN_SCRIPTING,ST_PHP_LINE_COMMENT>"</script>"{WHITESPACE}? {
        popState();
	return PHPTokenId.T_INLINE_HTML;
}

<ST_PHP_IN_SCRIPTING>"%>"{WHITESPACE}? {
	if (asp_tags) {
            yybegin(YYINITIAL);
            stack.clear();
	    return PHPTokenId.PHP_CLOSETAG;
	}
	return  PHPTokenId.UNKNOWN_TOKEN;
}

<ST_PHP_LINE_COMMENT>"%>"{WHITESPACE}? {
	if (asp_tags) {
            yybegin(YYINITIAL);
            stack.clear();
	    return PHPTokenId.PHP_CLOSETAG;
	}
	String text = yytext();
	if(text.indexOf('\r') != -1 || text.indexOf('\n') != -1 ){
		popState();
	}
	return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_IN_SCRIPTING>(b?[\"]{DOUBLE_QUOTES_CHARS}*("{"*|"$"*)[\"]) {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>(b?[']([^'\\]|("\\"{ANY_CHAR}))*[']) {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>b?[\"] {
    pushState(ST_PHP_DOUBLE_QUOTES);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}[']{LABEL}[']{NEWLINE} {
	int bprefix = (yytext().charAt(0) != '<') ? 1 : 0;
        int startString=3+bprefix;
        /* 3 is <<<, 2 is quotes, 1 is newline */
        heredoc_len = yylength()-bprefix-3-2-1-(yytext().charAt(yylength()-2)=='\r'?1:0);
        while ((yytext().charAt(startString) == ' ') || (yytext().charAt(startString) == '\t')) {
            startString++;
            heredoc_len--;
        }
        // first quate
        startString++;
        heredoc = yytext().substring(startString, heredoc_len+startString);
        yybegin(ST_PHP_START_NOWDOC);
        return PHPTokenId.PHP_NOWDOC_TAG;
}

<ST_PHP_START_NOWDOC>{ANY_CHAR} {
	yypushback(1);
	yybegin(ST_PHP_NOWDOC);
}

<ST_PHP_START_NOWDOC>{LABEL}";"?[\r\n] {
    int label_len = yylength() - 1;

    if (yytext().charAt(label_len-1)==';') {
        label_len--;
    }

    if (label_len==heredoc_len && yytext().substring(0,label_len).equals(heredoc)) {
        heredoc=null;
        heredoc_len=0;
        yybegin(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_NOWDOC_TAG;
    } else {
        return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
    }
}

               
<ST_PHP_NOWDOC>{NOWDOC_CHARS}*{NEWLINE}+{LABEL}";"?[\n\r] {
    int label_len = yylength() - 1;

    if (yytext().charAt(label_len-1)==';') {
	   label_len--;
    }
    if (label_len > heredoc_len && yytext().substring(label_len - heredoc_len,label_len).equals(heredoc)) {
        yybegin(ST_PHP_END_NOWDOC);
    }
    yypushback(1);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_END_NOWDOC>{ANY_CHAR} {
    heredoc=null; heredoc_len=0;
    yybegin(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}
                     
<ST_PHP_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}({LABEL}|"\""{LABEL}"\""){NEWLINE} {
    int bprefix = (yytext().charAt(0) != '<') ? 1 : 0;
    int startString=3+bprefix;
    heredoc_len = yylength()-bprefix-3-1-(yytext().charAt(yylength()-2)=='\r'?1:0);
    while ((yytext().charAt(startString) == ' ') || (yytext().charAt(startString) == '\t')) {
        startString++;
        heredoc_len--;
    }
    // HEREDOC PHP 5.3
    if (yytext().charAt(startString) == '"') {
        heredoc_len -= 2;
        startString ++;
    }
    heredoc = yytext().substring(startString,heredoc_len+startString);
    yybegin(ST_PHP_START_HEREDOC);
    return PHPTokenId.PHP_HEREDOC_TAG;
}

<ST_PHP_IN_SCRIPTING>[`] {
    pushState(ST_PHP_BACKQUOTE);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_START_HEREDOC>{ANY_CHAR} {
	yypushback(1);
	yybegin(ST_PHP_HEREDOC);
}

<ST_PHP_START_HEREDOC>{LABEL}";"?[\n\r] {
    int label_len = yylength() - 1;

    if (yytext().charAt(label_len-1)==';') {
	    label_len--;
    }

    if (label_len==heredoc_len && yytext().substring(0,label_len).equals(heredoc)) {
        heredoc=null;
        heredoc_len=0;
        yybegin(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_HEREDOC_TAG;
    } else {
        return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
    }
}

<ST_PHP_HEREDOC>{HEREDOC_CHARS}*{HEREDOC_NEWLINE}+{LABEL}";"?[\n\r] {
    int label_len = yylength() - 1;

    if (yytext().charAt(label_len-1)==';') {
	   label_len--;
    }
    if (label_len > heredoc_len && yytext().substring(label_len - heredoc_len,label_len).equals(heredoc)) {
    	   yypushback(1);
        yybegin(ST_PHP_END_HEREDOC);
    }
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_END_HEREDOC>{ANY_CHAR} {
    heredoc=null;
    heredoc_len=0;
    yybegin(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_QUOTES_AFTER_VARIABLE>"{$" {
    yypushback(1);
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_CURLY_OPEN;
}

<ST_PHP_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}+ {
	return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {DOUBLE_QUOTES_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[\"]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[\"])) {
    yypushback(1);
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_BACKQUOTE>{BACKQUOTE_CHARS}+ {
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {BACKQUOTE_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[`]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_BACKQUOTE>{BACKQUOTE_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[`])) {
	yypushback(1);
	return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)? {
	return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?("{"{2,}|"$"{2,})
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?("{""{"+|"$""$"+) {
    yypushback(1);
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_DOUBLE_QUOTES>[\"] {
    popState();
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_BACKQUOTE>[`] {
    popState();
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_DOUBLE_QUOTES>. {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_BACKQUOTE>. {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

/* ============================================
   Stay in this state until we find a whitespace.
   After we find a whitespace we go the the prev state and try again from the next token.
   ============================================ */
<ST_PHP_HIGHLIGHTING_ERROR> {
	{WHITESPACE}	{popState();return PHPTokenId.WHITESPACE;}
    .   	        {return  PHPTokenId.UNKNOWN_TOKEN;}
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<ST_PHP_IN_SCRIPTING,ST_PHP_DOUBLE_QUOTES,ST_PHP_VAR_OFFSET,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_START_HEREDOC,ST_PHP_END_HEREDOC,ST_PHP_NOWDOC,ST_PHP_START_NOWDOC,ST_PHP_END_NOWDOC>. {
    yypushback(1);
    pushState(ST_PHP_HIGHLIGHTING_ERROR);
}
