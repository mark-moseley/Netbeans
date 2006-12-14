/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.el.lexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for Expression Language.
 * It does NOT recognizes the EL delimiters ${ } and #{ }
 *
 * @author Petr Pisl
 * @author Marek Fukala
 *
 * @version 1.00
 */

public class ELLexer implements Lexer<ELTokenId> {
    
    private static final Logger LOGGER = Logger.getLogger(ELLexer.class.getName());
    private static final boolean LOG = Boolean.getBoolean("j2ee_lexer_debug"); //NOI18N
    
    private static final int EOF = LexerInput.EOF;
    
    private final LexerInput input;
    
    private final TokenFactory<ELTokenId> tokenFactory;
    
    public Object state() {
        return lexerState;
    }
    
    /** Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first usage,
     * this will be overwritten with state, which originated transition to
     * charref subanalyzer.
     */
    private int lexerState = INIT;
    
    
    /* Internal states used internally by analyzer. There
     * can be any number of them declared by the analyzer.
     */
    private static final int INIT = 1; //initial lexer state
    private static final int ISI_IDENTIFIER = 2;
    private static final int ISI_CHAR = 3; // inside char constant
    private static final int ISI_CHAR_A_BSLASH = 4; // inside char constant after backslash
    private static final int ISI_STRING = 5; // inside a string " ... "
    private static final int ISI_STRING_A_BSLASH = 6; // inside string "..." constant after backslash
    private static final int ISI_CHAR_STRING = 7;  // inside a string '...'
    private static final int ISI_CHAR_STRING_A_BSLASH = 8; // inside string '...'contant after backslash
    private static final int ISA_ZERO = 9; // after '0'
    private static final int ISI_INT = 10; // integer number
    private static final int ISI_OCTAL = 11; // octal number
    private static final int ISI_DOUBLE = 12; // double number
    private static final int ISI_DOUBLE_EXP = 13; // double number
    private static final int ISI_HEX = 14; // hex number
    private static final int ISA_DOT = 15; // after '.'
    private static final int ISI_WHITESPACE = 16; // inside white space
    private static final int ISA_EQ = 17; // after '='
    private static final int ISA_GT = 18; // after '>'
    private static final int ISA_LT = 19; // after '<'
    //private static final int ISA_PLUS = 20; // after '+'
    //private static final int ISA_MINUS = 21; // after '-'
    //private static final int ISA_STAR = 22; // after '*'
    private static final int ISA_PIPE = 23; // after '|'
    private static final int ISA_AND = 24; // after '&'
    private static final int ISA_EXCLAMATION = 25; // after '!'
    private static final int ISI_BRACKET = 26; // after '['
    private static final int ISI_BRACKET_A_WHITESPACE = 27;
    private static final int ISI_BRACKET_A_IDENTIFIER = 28;
    private static final int ISI_BRACKET_ISA_EQ = 29;
    private static final int ISI_BRACKET_ISA_GT = 30;
    private static final int ISI_BRACKET_ISA_LT =31;
    private static final int ISI_BRACKET_ISA_PIPE = 32; // after '|'
    private static final int ISI_BRACKET_ISA_AND = 33; // after '&'
    private static final int ISI_BRACKET_ISA_ZERO = 34; // after '0'
    private static final int ISI_BRACKET_ISA_DOT = 35; // after '.'
    private static final int ISI_BRACKET_ISI_INT = 36; // after '.'
    private static final int ISI_BRACKET_ISI_OCTAL = 37; // octal number
    private static final int ISI_BRACKET_ISI_DOUBLE = 38; // double number
    private static final int ISI_BRACKET_ISI_DOUBLE_EXP = 39; // double number
    private static final int ISI_BRACKET_ISI_HEX = 40; // hex number
    private static final int ISI_DOULE_EXP_ISA_SIGN = 41;
    private static final int ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN = 42;
    //private static final int ISA_PERCENT = 24; // after '%'
    
    
    public ELLexer(LexerRestartInfo<ELTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.lexerState = INIT;
        } else {
            this.lexerState = ((Integer) info.state()).intValue();
        }
    }
    
    
    /** This is core function of analyzer and it returns one of following numbers:
     * a) token number of next token from scanned text
     * b) EOL when end of line was found in scanned buffer
     * c) EOT when there is no more chars available in scanned buffer.
     *
     * The function scans the active character and does one or more
     * of the following actions:
     * 1. change internal analyzer state (state = new-state)
     * 2. return token ID (return token-ID)
     * 3. adjust current position to signal different end of token;
     *    the character that offset points to is not included in the token
     */
    public Token<ELTokenId> nextToken() {
        
        int actChar;
        
        while (true) {
            actChar = input.read();
            
            if (actChar == EOF) {
                if(input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    //there is something else in the buffer except EOL
                    //we will return last token now
                    input.backup(1); //backup the EOL, we will return null in next nextToken() call
                    break;
                }
            }
            
            switch (lexerState) { // switch by the current internal state
                case INIT:
                    
                    switch (actChar) {
                        case '"':
                            lexerState = ISI_STRING;
                            break;
                        case '\'':
                            lexerState = ISI_CHAR;
                            break;
                        case '/':
                            return token(ELTokenId.DIV);
                        case '=':
                            lexerState = ISA_EQ;
                            break;
                        case '>':
                            lexerState = ISA_GT;
                            break;
                        case '<':
                            lexerState = ISA_LT;
                            break;
                        case '+':
                            return token(ELTokenId.PLUS);
                        case '-':
                            return token(ELTokenId.MINUS);
                        case '*':
                            return token(ELTokenId.MUL);
                        case '|':
                            lexerState = ISA_PIPE;
                            break;
                        case '&':
                            lexerState = ISA_AND;
                            break;
                        case '[':
                            return token(ELTokenId.LBRACKET);
                        case ']':
                            return token(ELTokenId.RBRACKET);
                        case '%':
                            return token(ELTokenId.MOD);
                        case ':':
                            return token(ELTokenId.COLON);
                        case '!':
                            lexerState = ISA_EXCLAMATION;
                            break;
                        case '(':
                            return token(ELTokenId.LPAREN);
                        case ')':
                            return token(ELTokenId.RPAREN);
                        case ',':
                            return token(ELTokenId.COMMA);
                        case '?':
                            return token(ELTokenId.QUESTION);
                        case '\n':
                            return token(ELTokenId.EOL);
                        case '0':
                            lexerState = ISA_ZERO;
                            break;
                        case '.':
                            lexerState = ISA_DOT;
                            break;
                        default:
                            // Check for whitespace
                            if (Character.isWhitespace(actChar)) {
                                lexerState = ISI_WHITESPACE;
                                break;
                            }
                            
                            // check whether it can be identifier
                            if (Character.isJavaIdentifierStart(actChar)){
                                lexerState = ISI_IDENTIFIER;
                                break;
                            }
                            // Check for digit
                            if (Character.isDigit(actChar)) {
                                lexerState = ISI_INT;
                                break;
                            }
                            return token(ELTokenId.INVALID_CHAR);
                            //break;
                    }
                    break;
                    
                    
                case ISI_WHITESPACE: // white space
                    if (!Character.isWhitespace(actChar)) {
                        lexerState = INIT;
                        input.backup(1);
                        return token(ELTokenId.WHITESPACE);
                    }
                    break;
                    
                case ISI_BRACKET:
                    switch (actChar){
                        case ']':
                            lexerState = INIT;
                            input.backup(1);
                            return token(ELTokenId.IDENTIFIER);
                        case '"':
                            return token(ELTokenId.LBRACKET);
                        case '\'':
                            return token(ELTokenId.LBRACKET);
                        case '/':
                            return token(ELTokenId.DIV);
                        case '+':
                            return token(ELTokenId.PLUS);
                        case '-':
                            return token(ELTokenId.MINUS);
                        case '*':
                            return token(ELTokenId.MUL);
                        case '[':
                            return token(ELTokenId.LBRACKET);
                        case '%':
                            return token(ELTokenId.MOD);
                        case ':':
                            return token(ELTokenId.COLON);
                        case '(':
                            return token(ELTokenId.LPAREN);
                        case ')':
                            return token(ELTokenId.RPAREN);
                        case ',':
                            return token(ELTokenId.COMMA);
                        case '?':
                            return token(ELTokenId.QUESTION);
                        case '=':
                            lexerState = ISI_BRACKET_ISA_EQ;
                            break;
                        case '>':
                            lexerState = ISI_BRACKET_ISA_GT;
                            break;
                        case '<':
                            lexerState = ISI_BRACKET_ISA_LT;
                            break;
                        case '|':
                            lexerState = ISI_BRACKET_ISA_PIPE;
                            break;
                        case '&':
                            lexerState = ISI_BRACKET_ISA_AND;
                            break;
                        case '0':
                            lexerState = ISI_BRACKET_ISA_ZERO;
                            break;
                        case '.':
                            lexerState = ISI_BRACKET_ISA_DOT;
                            break;
                        default :
                            // Check for whitespace
                            if (Character.isWhitespace(actChar)) {
                                lexerState = ISI_BRACKET_A_WHITESPACE;
                                break;
                            }
                            if (Character.isJavaIdentifierStart(actChar)){
                                // - System.out.print(" state->ISI_IDENTIFIER ");
                                lexerState = ISI_BRACKET_A_IDENTIFIER;
                                break;
                            }
                            // Check for digit
                            if (Character.isDigit(actChar)) {
                                lexerState = ISI_BRACKET_ISI_INT;
                                break;
                            }
                            return token(ELTokenId.INVALID_CHAR);
                            //break;
                    }
                    break;
                    
                case ISI_BRACKET_A_WHITESPACE:
                    if (!Character.isWhitespace(actChar)) {
                        lexerState = ISI_BRACKET;
                        input.backup(1);
                        return token(ELTokenId.WHITESPACE);
                    }
                    break;
                    
                case ISI_BRACKET_ISA_EQ:
                case ISA_EQ:
                    switch (actChar) {
                        case '=':
                            lexerState = INIT;
                            return  token(ELTokenId.EQ_EQ);
                        default:
                            lexerState = (lexerState == ISI_BRACKET_ISA_EQ) ? ISI_BRACKET : INIT;
                            input.backup(1);
                            break;
                    }
                    break;
                    
                case ISI_BRACKET_ISA_GT:
                case ISA_GT:
                    switch (actChar) {
                        case '=':
                            lexerState = INIT;
                            return token(ELTokenId.GT_EQ);
                        default:
                            lexerState = (lexerState == ISI_BRACKET_ISA_GT) ? ISI_BRACKET : INIT;
                            input.backup(1);
                            return token(ELTokenId.GT);
                    }
                    //break;
                case ISI_BRACKET_ISA_LT:
                case ISA_LT:
                    switch (actChar) {
                        case '=':
                            lexerState = INIT;
                            return token(ELTokenId.LT_EQ);
                        default:
                            lexerState = (lexerState == ISI_BRACKET_ISA_LT) ? ISI_BRACKET : INIT;
                            input.backup(1);
                            return token(ELTokenId.LT);
                    }
                    //break;
                case ISI_BRACKET_ISA_PIPE:
                case ISA_PIPE:
                    switch (actChar) {
                        case '|':
                            lexerState = INIT;
                            return token(ELTokenId.OR_OR);
                        default:
                            lexerState = (lexerState == ISI_BRACKET_ISA_PIPE) ? ISI_BRACKET : INIT;
                            input.backup(1);
                            break;
                    }
                    break;
                case ISI_BRACKET_ISA_AND:
                case ISA_AND:
                    switch (actChar) {
                        case '&':
                            lexerState = INIT;
                            return token(ELTokenId.AND_AND);
                        default:
                            lexerState = (lexerState == ISI_BRACKET_ISA_AND) ? ISI_BRACKET : INIT;
                            input.backup(1);
                            break;
                    }
                    break;
                case ISA_EXCLAMATION:
                    switch (actChar) {
                        case '=':
                            lexerState = INIT;
                            return token(ELTokenId.NOT_EQ);
                        default:
                            lexerState = INIT;
                            input.backup(1);
                            return token(ELTokenId.NOT);
                    }
                case ISI_STRING:
                    switch (actChar) {
                        case '\\':
                            lexerState = ISI_STRING_A_BSLASH;
                            break;
                        case '\n':
                            lexerState = INIT;
                            input.backup(1);
                            return token(ELTokenId.STRING_LITERAL);
                        case '"': // NOI18N
                            lexerState = INIT;
                            return token(ELTokenId.STRING_LITERAL);
                    }
                    break;
                case ISI_STRING_A_BSLASH:
                    lexerState = ISI_STRING;
                    break;
                case ISI_BRACKET_A_IDENTIFIER:
                case ISI_IDENTIFIER:
                    if (!(Character.isJavaIdentifierPart(actChar))){
                        switch (lexerState){
                            case ISI_IDENTIFIER:
                                lexerState = INIT; break;
                            case ISI_BRACKET_A_IDENTIFIER:
                                lexerState = ISI_BRACKET;
                                break;
                        }
                        Token<ELTokenId> tid = matchKeyword(input);
                        input.backup(1);
                        if (tid == null){
                            if (actChar == ':'){
                                tid = token(ELTokenId.TAG_LIB_PREFIX);
                            } else{
                                tid = token(ELTokenId.IDENTIFIER);
                            }
                        }
                        return tid;
                    }
                    break;
                    
                case ISI_CHAR:
                    switch (actChar) {
                        case '\\':
                            lexerState = ISI_CHAR_A_BSLASH;
                            break;
                        case '\n':
                            lexerState = INIT;
                            input.backup(1);
                            return token(ELTokenId.CHAR_LITERAL);
                        case '\'':
                            lexerState = INIT;
                            return token(ELTokenId.CHAR_LITERAL);
                        default :
                            char prevChar = input.readText().charAt(input.readLength() - 1);
                            if (prevChar != '\'' && prevChar != '\\'){
                                lexerState = ISI_CHAR_STRING;
                            }
                    }
                    break;
                    
                case ISI_CHAR_A_BSLASH:
                    switch (actChar) {
                        case '\'':
                        case '\\':
                            break;
                        default:
                            input.backup(1);
                            break;
                    }
                    lexerState = ISI_CHAR;
                    break;
                    
                case ISI_CHAR_STRING:
                    // - System.out.print(" ISI_CHAR_STRING (");
                    switch (actChar) {
                        case '\\':
                            // - System.out.print(" state->ISI_CHAR_A_BSLASH )");
                            lexerState = ISI_CHAR_STRING_A_BSLASH;
                            break;
                        case '\n':
                            lexerState = INIT;
                            input.backup(1);
                            return token(ELTokenId.STRING_LITERAL);
                        case '\'':
                            lexerState = INIT;
                            return token(ELTokenId.STRING_LITERAL);
                    }
                    // - System.out.print(")");
                    break;
                    
                case ISI_CHAR_STRING_A_BSLASH:
                    switch (actChar) {
                        case '\'':
                        case '\\':
                            break;
                        default:
                            input.backup(1);
                            break;
                    }
                    lexerState = ISI_CHAR_STRING;
                    break;
                    
                case ISI_BRACKET_ISA_ZERO:
                case ISA_ZERO:
                    switch (actChar) {
                        case '.':
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_DOUBLE : ISI_DOUBLE;
                            break;
                        case 'x':
                        case 'X':
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_HEX : ISI_HEX;
                            break;
                        case 'l':
                        case 'L':
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.LONG_LITERAL);
                        case 'f':
                        case 'F':
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.FLOAT_LITERAL);
                        case 'd':
                        case 'D':
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.DOUBLE_LITERAL);
                        case '8': // it's error to have '8' and '9' in octal number
                        case '9':
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.INVALID_OCTAL_LITERAL);
                        case 'e':
                        case 'E':
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_DOUBLE_EXP : ISI_DOUBLE_EXP;
                            break;
                        default:
                            if (Character.isDigit(actChar)) { // '8' and '9' already handled
                                lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_OCTAL : ISI_OCTAL;
                                break;
                            }
                            lexerState = (lexerState == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                            input.backup(1);
                            return token(ELTokenId.INT_LITERAL);
                    }
                    break;
                    
                case ISI_BRACKET_ISI_INT:
                case ISI_INT:
                    switch (actChar) {
                        case 'l':
                        case 'L':
                            lexerState = (lexerState == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.LONG_LITERAL);
                        case '.':
                            lexerState = (lexerState == ISI_BRACKET_ISI_INT) ? ISI_BRACKET_ISI_DOUBLE : ISI_DOUBLE;
                            break;
                        case 'f':
                        case 'F':
                            lexerState = (lexerState == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.FLOAT_LITERAL);
                        case 'd':
                        case 'D':
                            lexerState = (lexerState == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.DOUBLE_LITERAL);
                        case 'e':
                        case 'E':
                            lexerState = ISI_DOUBLE_EXP;
                            break;
                        default:
                            if (!(actChar >= '0' && actChar <= '9')) {
                                lexerState = (lexerState == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                                input.backup(1);
                                return token(ELTokenId.INT_LITERAL);
                            }
                    }
                    break;
                    
                case ISI_BRACKET_ISI_OCTAL:
                case ISI_OCTAL:
                    if (!(actChar >= '0' && actChar <= '7')) {
                        lexerState = (lexerState == ISI_BRACKET_ISI_OCTAL) ? ISI_BRACKET : INIT;
                        input.backup(1);
                        return token(ELTokenId.OCTAL_LITERAL);
                    }
                    break;
                    
                case ISI_BRACKET_ISI_DOUBLE:
                case ISI_DOUBLE:
                    switch (actChar) {
                        case 'f':
                        case 'F':
                            lexerState = (lexerState == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.FLOAT_LITERAL);
                        case 'd':
                        case 'D':
                            lexerState = (lexerState == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.DOUBLE_LITERAL);
                        case 'e':
                        case 'E':
                            lexerState = (lexerState == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET_ISI_DOUBLE_EXP : ISI_DOUBLE_EXP;
                            break;
                        default:
                            if (!((actChar >= '0' && actChar <= '9')
                            || actChar == '.')) {
                                lexerState = (lexerState == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET : INIT;
                                input.backup(1);
                                return token(ELTokenId.DOUBLE_LITERAL);
                            }
                    }
                    break;
                    
                case ISI_DOUBLE_EXP:
                case ISI_BRACKET_ISI_DOUBLE_EXP:
                    switch (actChar) {
                        case 'f':
                        case 'F':
                            lexerState = (lexerState == ISI_BRACKET_ISI_DOUBLE_EXP) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.FLOAT_LITERAL);
                        case 'd':
                        case 'D':
                            lexerState = (lexerState == ISI_BRACKET_ISI_DOUBLE_EXP) ? ISI_BRACKET : INIT;
                            return token(ELTokenId.DOUBLE_LITERAL);
                        case '-':
                        case '+':
                            lexerState = ISI_DOULE_EXP_ISA_SIGN;
                            break;
                        default:
                            if (!Character.isDigit(actChar)){
                                //|| ch == '-' || ch == '+')) {
                                lexerState = (lexerState == ISI_BRACKET_ISI_DOUBLE_EXP) ? ISI_BRACKET : INIT;
                                input.backup(1);
                                return token(ELTokenId.DOUBLE_LITERAL);
                            }
                    }
                    break;
                    
                case ISI_DOULE_EXP_ISA_SIGN:
                case ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN:
                    if (!Character.isDigit(actChar)){
                        lexerState = (lexerState == ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN) ? ISI_BRACKET : INIT;
                        input.backup(1);
                        return token(ELTokenId.DOUBLE_LITERAL);
                    }
                    break;
                    
                case ISI_BRACKET_ISI_HEX:
                case ISI_HEX:
                    if (!((actChar >= 'a' && actChar <= 'f')
                    || (actChar >= 'A' && actChar <= 'F')
                    || Character.isDigit(actChar))
                    ) {
                        lexerState = (lexerState == ISI_BRACKET_ISI_HEX) ? ISI_BRACKET : INIT;
                        input.backup(1);
                        return token(ELTokenId.HEX_LITERAL);
                    }
                    break;
                    
                case ISI_BRACKET_ISA_DOT:
                case ISA_DOT:
                    if (Character.isDigit(actChar)) {
                        lexerState = (lexerState == ISI_BRACKET_ISA_DOT) ? ISI_BRACKET_ISI_DOUBLE : ISI_DOUBLE;
                        
                    } else { // only single dot
                        lexerState = (lexerState == ISI_BRACKET_ISA_DOT) ? ISI_BRACKET : INIT;
                        input.backup(1);
                        return token(ELTokenId.DOT);
                    }
                    break;
                    
            } // end of switch(state)
            
        } //end of big while
        
        /** At this stage there's no more text in the scanned buffer.
         * Scanner first checks whether this is completely the last
         * available buffer.
         */
        switch (lexerState) {
            case INIT:
                if (input.readLength() == 0) {
                    return null;
                }
                break;
            case ISI_WHITESPACE:
                lexerState = INIT;
                return token(ELTokenId.WHITESPACE);
            case ISI_IDENTIFIER:
                lexerState = INIT;
                Token<ELTokenId> kwd = matchKeyword(input);
                return (kwd != null) ? kwd : token(ELTokenId.IDENTIFIER);
            case ISI_STRING:
            case ISI_STRING_A_BSLASH:
                return token(ELTokenId.STRING_LITERAL); // hold the state
            case ISI_CHAR:
            case ISI_CHAR_A_BSLASH:
                return token(ELTokenId.CHAR_LITERAL);
            case ISI_CHAR_STRING :
            case ISI_CHAR_STRING_A_BSLASH :
                return token(ELTokenId.STRING_LITERAL);
            case ISA_ZERO:
            case ISI_INT:
                lexerState = INIT;
                return token(ELTokenId.INT_LITERAL);
            case ISI_OCTAL:
                lexerState = INIT;
                return token(ELTokenId.OCTAL_LITERAL);
            case ISI_DOUBLE:
            case ISI_DOUBLE_EXP:
            case ISI_DOULE_EXP_ISA_SIGN:
            case ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN:
                lexerState = INIT;
                return token(ELTokenId.DOUBLE_LITERAL);
            case ISI_HEX:
                lexerState = INIT;
                return token(ELTokenId.HEX_LITERAL);
            case ISA_DOT:
                lexerState = INIT;
                return token(ELTokenId.DOT);
            case ISA_EQ:
                lexerState = INIT;
                return token(ELTokenId.EQ_EQ);
            case ISA_GT:
                lexerState = INIT;
                return token(ELTokenId.GT);
            case ISA_LT:
                lexerState = INIT;
                return token(ELTokenId.LT);
            case ISA_PIPE:
                lexerState = INIT;
                return token(ELTokenId.OR_OR);
            case ISA_AND:
                lexerState = INIT;
                return token(ELTokenId.AND_AND);
            case ISA_EXCLAMATION:
                lexerState = INIT;
                return token(ELTokenId.NOT);
            case ISI_BRACKET:
            case ISI_BRACKET_A_IDENTIFIER:
                lexerState = INIT;
                return token(ELTokenId.IDENTIFIER);
            case ISI_BRACKET_A_WHITESPACE:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.WHITESPACE);
            case ISI_BRACKET_ISA_EQ:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.EQ_EQ);
            case ISI_BRACKET_ISA_GT:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.GT_EQ);
            case ISI_BRACKET_ISA_LT:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.LT_EQ);
            case ISI_BRACKET_ISA_AND:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.AND_AND);
            case ISI_BRACKET_ISA_PIPE:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.OR_OR);
            case ISI_BRACKET_ISA_DOT:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.DOT);
            case ISI_BRACKET_ISA_ZERO:
            case ISI_BRACKET_ISI_INT:
                lexerState = ISI_BRACKET;
                return token(ELTokenId.INT_LITERAL);
        }
        
        
        return null;
    }
    
    
    public Token<ELTokenId> matchKeyword(LexerInput lexerInput) {
        int len = lexerInput.readLength();
        char[] buffer = new char[len];
        String read = lexerInput.readText().toString();
        read.getChars(0, read.length(), buffer, 0);
        int offset = 0;
        
        if (len > 10)
            return null;
        if (len <= 1)
            return null;
        switch (buffer[offset++]) {
            case 'a':
                if (len <= 2) return null;
                return (len == 3
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'd')
                        ? token(ELTokenId.AND_KEYWORD) : null;
            case 'd':
                if (len <= 2) return null;
                return (len == 3
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'v')
                        ? token(ELTokenId.DIV_KEYWORD) : null;
            case 'e':
                switch (buffer[offset++]) {
                    case 'q':
                        return (len == 2) ? token(ELTokenId.EQ_KEYWORD) : null;
                    case 'm':
                        return (len == 5
                                && buffer[offset++] == 'p'
                                && buffer[offset++] == 't'
                                && buffer[offset++] == 'y')
                                ? token(ELTokenId.EMPTY_KEYWORD) : null;
                    default:
                        return null;
                }
            case 'f':
                return (len == 5
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 'e')
                        ? token(ELTokenId.FALSE_KEYWORD) : null;
            case 'g':
                switch (buffer[offset++]){
                    case 'e':
                        return (len == 2) ? token(ELTokenId.GE_KEYWORD) : null;
                    case 't':
                        return (len == 2) ? token(ELTokenId.GT_KEYWORD) : null;
                    default:
                        return null;
                }
            case 'l':
                switch (buffer[offset++]){
                    case 'e':
                        return (len == 2) ? token(ELTokenId.LE_KEYWORD) : null;
                    case 't':
                        return (len == 2) ? token(ELTokenId.LT_KEYWORD) : null;
                    default:
                        return null;
                }
            case 'i':
                if (len <= 9) return null;
                return (len == 10
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'f')
                        ? token(ELTokenId.INSTANCEOF_KEYWORD) : null;
            case 'm':
                if (len <= 2) return null;
                return (len == 3
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'd')
                        ? token(ELTokenId.MOD_KEYWORD) : null;
            case 'n':
                switch (buffer[offset++]){
                    case 'e':
                        return (len == 2) ? token(ELTokenId.NE_KEYWORD) : null;
                    case 'o':
                        return (len == 3
                                && buffer[offset++] == 't')
                                ? token(ELTokenId.NOT_KEYWORD) : null;
                    case 'u':
                        return (len == 4
                                && buffer[offset++] == 'l'
                                && buffer[offset++] == 'l')
                                ? token(ELTokenId.NULL_KEYWORD) : null;
                    default:
                        return null;
                }
            case 'o':
                return (len == 2
                        && buffer[offset++] == 'r')
                        ? token(ELTokenId.OR_KEYWORD) : null;
            case 't':
                return (len == 4
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 'e')
                        ? token(ELTokenId.TRUE_KEYWORD) : null;
                
            default :
                return null;
        }
    }
    
    private Token<ELTokenId> token(ELTokenId tokenId) {
        if(LOG) {
            if(input.readLength() == 0) {
                LOGGER.log(Level.INFO, "[" + this.getClass().getSimpleName() + "] Found zero length token: "); //NOI18N
            }
            LOGGER.log(Level.INFO, "[" + this.getClass().getSimpleName() + "] token ('" + input.readText().toString() + "'; id=" + tokenId + ")\n"); //NOI18N
        }
        return tokenFactory.createToken(tokenId);
    }
    
    public void release() {
    }

}
