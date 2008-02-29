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

package org.netbeans.modules.cnd.editor.reformat;

import java.util.LinkedList;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.reformat.BracesStack.StatementKind;
import org.netbeans.modules.cnd.editor.reformat.DiffLinkedList.DiffResult;
import org.netbeans.modules.cnd.editor.reformat.Reformatter.Diff;

/**
 *
 * @author Alexander Simon
 */
public class ReformatterImpl {
    /*package local*/ final ExtendedTokenSequence ts;
    /*package local*/ final CodeStyle codeStyle;
    /*package local*/ final DiffLinkedList diffs = new DiffLinkedList();
    /*package local*/ final BracesStack braces = new BracesStack();
    private final int startOffset;
    private final int endOffset;
    private PreprocessorFormatter preprocessorFormatter;
    
    ReformatterImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset, CodeStyle codeStyle){
        this.ts          = new ExtendedTokenSequence(ts, diffs);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.codeStyle = codeStyle;
        preprocessorFormatter = new PreprocessorFormatter(this);
    }
    
    LinkedList<Diff> reformat(){
        ts.moveStart();
        Token<CppTokenId> previous = ts.lookPrevious();
        while(ts.moveNext()){
            if (ts.offset() > endOffset) {
                break;
            }
            Token<CppTokenId> current = ts.token();
            CppTokenId id = current.id();
            if (previous != null && previous.id() == PREPROCESSOR_DIRECTIVE && id != PREPROCESSOR_DIRECTIVE){
                // indent afre preprocessor directive
                if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                    braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                }
                if (doFormat()){
                    indentNewLine(current);
                }
            }
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                case NEW_LINE:
                case WHITESPACE:
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                case LINE_COMMENT:
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                case COLON:
                case SEMICOLON:
                case LBRACE:
                case RBRACE:
                    break;
                default:
                    braces.setLastStatementStart(ts);
            }
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                {
                    preprocessorFormatter.indentPreprocessor(previous);
                    break;
                }
                case NEW_LINE:
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                        braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                    }
                    if (doFormat()) {
                        newLineFormat(previous, current, braces.parenDepth);
                    }
                    break;
                }
                case WHITESPACE:
                {
                    if (doFormat()) {
                        whiteSpaceFormat(previous, current);
                    }
                    break;
                }
                case DOXYGEN_COMMENT:
                case BLOCK_COMMENT:
                {
                    if (doFormat()) {
                        reformatBlockComment(previous, current);
                    }
                    break;
                }
                case LBRACE: //("{", "separator"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        braceFormat(previous, current);
                    }
                    break;
                }
                case LPAREN: //("(", "separator"),
                {
                    if (braces.parenDepth == 0) {
                        if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                            braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                        }
                    }
                    braces.parenDepth++;
                    if (doFormat()) {
                        formatLeftParen(previous, current);
                    }
                    break;
                }
                case RPAREN: //(")", "separator"),
                {
                    braces.parenDepth--;
                    if (braces.parenDepth == 0) {
                        StackEntry entry = braces.peek();
                        if (entry == null || entry.getKind() != LBRACE ||
                            entry.getImportantKind() == CLASS || entry.getImportantKind() == NAMESPACE){
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                        }
                    }
                    if (doFormat()) {
                        formatRightParen(previous, current);
                    }
                    break;
                }
                case IDENTIFIER:
                {
                    boolean isStart = false;
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                        isStart = ts.index() == braces.lastStatementStart;
                    }
                    if (isStart) {
                        Token<CppTokenId> next = ts.lookNextImportant();
                        if (next != null && next.id() == COLON) {
                            braces.isLabel = true;
                            if (doFormat()) {
                                if (!ts.isFirstLineToken()) {
                                    ts.addBeforeCurrent("\n");
                                } else {
                                    DiffResult diff = diffs.getDiffs(ts, -1);
                                    if (diff == null){
                                        if (previous != null && previous.id() == WHITESPACE) {
                                            ts.replacePrevious(previous, "");
                                        }
                                    } else {
                                        if (diff.after != null) {
                                            diff.after.replaceSpaces("");
                                        }
                                        if (diff.replace != null) {
                                            diff.replace.replaceSpaces("");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case SEMICOLON: //(";", "separator"),
                {
                    StackEntry entry = braces.peek();
                    if (braces.parenDepth == 0) {
                        braces.pop(ts);
                    }
                    if (entry != null && 
                       (entry.getKind() == DO || entry.getImportantKind() == DO)) {
                        Token<CppTokenId> next = ts.lookNextImportant();
                        if (next != null && next.id() == WHILE) {
                            braces.isDoWhile = true;
                        }
                    }
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeSemi());
                        if (true) {
                            // TODO should be controlled
                            // add new line after ;
                            if (braces.parenDepth == 0) {
                                Token<CppTokenId> next = ts.lookNext();
                                if (next != null) {
                                    Token<CppTokenId> n2 = ts.lookNext(2);
                                    if (!(next.id() == NEW_LINE ||
                                          next.id() == LINE_COMMENT ||
                                          next.id() == WHITESPACE && n2 != null && n2.id() == LINE_COMMENT)){
                                        ts.addAfterCurrent(current, getIndent("\n")); // NOI18N
                                        break;
                                    }    
                                }
                            }
                        }
                        spaceAfter(current, codeStyle.spaceAfterSemi());
                    }
                    if (braces.parenDepth == 0) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                    }
                    break;
                }
                case COMMA: //(",", "separator"),
                {
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeComma());
                        spaceAfter(current, codeStyle.spaceAfterComma());
                    }
                    break;
                }
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                {
                    StackEntry entry = braces.peek();
                    if (doFormat()) {
                        if (entry != null && entry.getImportantKind() != null){
                            switch (entry.getImportantKind()) {
                                case CLASS: //("class", "keyword"), //C++
                                case STRUCT: //("struct", "keyword"),
                                    Token<CppTokenId> next = ts.lookNextImportant();
                                    if (next != null && next.id() == COLON) {
                                        newLineBefore();
                                    }
                                    break;
                            }
                        }
                    }
                    break;
                }
                case COLON: //(":", "operator"),
                {
                    boolean isLabel = braces.isLabel;
                    braces.isLabel = false;
                    if (doFormat()) {
                        if (isLabel) {
                            spaceBefore(previous, false);
                            if(!ts.isLastLineToken()){
                                ts.addAfterCurrent(current, getIndent("\n")); // NOI18N
                            }
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            break;
                        }
                        Token<CppTokenId> p = ts.lookPreviousImportant();
                        if (p != null && 
                           (p.id() == PRIVATE ||
                            p.id() == PROTECTED ||
                            p.id() == PUBLIC)) {
                            spaceBefore(previous, false);
                            if(!ts.isLastLineToken()){
                                // TODO use flase?
                                ts.addAfterCurrent(current, getIndent("\n")); // NOI18N
                            }
                            break;
                        }
                        if (p != null && p.id() == DEFAULT) {
                            // TODO use flase?
                            spaceBefore(previous, false);
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            break;
                        }
                        Token<CppTokenId> p2 = ts.lookPreviousImportant(2);
                        if (p2 != null && p2.id() == CASE) {
                            // TODO use flase?
                            spaceBefore(previous, false);
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            break;
                        }
                        spaceBefore(previous, codeStyle.spaceBeforeColon());
                        spaceAfter(current, codeStyle.spaceAfterColon());
                    }
                    break;
                }
                case RBRACE: //("}", "separator"),
                {
                    StackEntry entry = braces.peek();
                    int indent = braces.pop(ts);
                    if (entry != null && 
                       (entry.getKind() == DO || entry.getImportantKind() == DO)) {
                        Token<CppTokenId> next = ts.lookNextImportant();
                        if (next != null && next.id() == WHILE) {
                            braces.isDoWhile = true;
                        }
                    }
                    if (doFormat()) {
                       boolean isClassDeclaration = entry != null && 
                                            entry.getImportantKind() != null &&
                                           (entry.getImportantKind() == CLASS ||
                                            entry.getImportantKind() == STRUCT ||
                                            entry.getImportantKind() == UNION ||
                                            entry.getImportantKind() == ENUM);
                        if (entry != null && entry.getImportantKind() == null) {
                            entry = braces.peek();
                            if (entry != null &&
                                entry.getImportantKind() != null && entry.getImportantKind() == SWITCH) {
                                indent--;
                            }
                        }
                        indentRbrace(entry, previous, indent, current, isClassDeclaration);
                    }
                    break;
                }
                case NOT: //("!", "operator"),
                case TILDE: //("~", "operator"),
                case PLUSPLUS: //("++", "operator"),
                case MINUSMINUS: //("--","operator"),
                {
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                        spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                    }
                    break;
                }
                case PLUS: //("+", "operator"),
                case MINUS: //("-", "operator"),
                {
                    if (doFormat()) {
                        OperatorKind kind = getOperatorKind(current);
                        if (kind == OperatorKind.BINARY){
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                        } else if (kind == OperatorKind.UNARY){
                            spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                            spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                        }
                    }
                    break;
                }
                case STAR: //("*", "operator"),
                case AMP: //("&", "operator"),
                {
                    if (doFormat()) {
                        OperatorKind kind = getOperatorKind(current);
                        if (kind == OperatorKind.BINARY){
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                        } else if (kind == OperatorKind.TYPE_MODIFIER){
                            //TODO style of type declaration
                        }
                    }
                    break;
                }
                case GT: //(">", "operator"),
                case LT: //("<", "operator"),
                {
                    if (doFormat()) {
                        OperatorKind kind = getOperatorKind(current);
                        if (kind == OperatorKind.BINARY){
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                        } else if (kind == OperatorKind.SEPARATOR){
                            //TODO style of template declaration
                        }
                    }
                    break;
                }
                case EQEQ: //("==", "operator"),
                case LTEQ: //("<=", "operator"),
                case GTEQ: //(">=", "operator"),
                case NOTEQ: //("!=","operator"),
                case AMPAMP: //("&&", "operator"),
                case BARBAR: //("||", "operator"),
                case SLASH: //("/", "operator"),
                case BAR: //("|", "operator"),
                case PERCENT: //("%", "operator"),
                case LTLT: //("<<", "operator"),
                case GTGT: //(">>", "operator"),
                {
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                        spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                    }
                    break;
                }
                case EQ: //("=", "operator"),
                case PLUSEQ: //("+=", "operator"),
                case MINUSEQ: //("-=", "operator"),
                case STAREQ: //("*=", "operator"),
                case SLASHEQ: //("/=", "operator"),
                case AMPEQ: //("&=", "operator"),
                case BAREQ: //("|=", "operator"),
                case CARETEQ: //("^=", "operator"),
                case PERCENTEQ: //("%=", "operator"),
                case LTLTEQ: //("<<=", "operator"),
                case GTGTEQ: //(">>=", "operator"),
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                    }
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceAroundAssignOps());
                        spaceAfter(current, codeStyle.spaceAroundAssignOps());
                    }
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                        braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                    }
                    break;
                }
                case NAMESPACE: //("namespace", "keyword"), //C++
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    break;
                }
                case IF: //("if", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeIfParen(), LPAREN);
                    }
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                       formatElse(previous);
                    }
                    break;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        boolean doSpaceBefore = true;
                        if (braces.isDoWhile) {
                            if (ts.isFirstLineToken()) {
                                if (!codeStyle.newLineWhile()) {
                                    // try to remove new line
                                    newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                                            codeStyle.spaceBeforeWhile(), false);
                                    doSpaceBefore = false;
                                }
                            } else {
                                if (codeStyle.newLineWhile()) {
                                    // add new line
                                    newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                                            codeStyle.spaceBeforeWhile(), false);
                                    doSpaceBefore = false;
                                }
                            }
                        }
                        if (doSpaceBefore){
                            spaceBefore(previous, codeStyle.spaceBeforeWhile());
                        }
                        spaceAfterBefore(current, codeStyle.spaceBeforeWhileParen(), LPAREN);
                    }
                    braces.isDoWhile = false;
                    break;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeForParen(), LPAREN);
                    }
                    break;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    braces.push(new StackEntry(ts));
                    break;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        boolean doSpaceBefore = true;
                        if (ts.isFirstLineToken()) {
                            if (!codeStyle.newLineCatch()){
                                // try to remove new line
                                newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                                        codeStyle.spaceBeforeCatch(), false);
                                doSpaceBefore = false;
                            }
                        } else {
                             if (codeStyle.newLineCatch()){
                                // add new line
                                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                                        codeStyle.spaceBeforeCatch(), false);
                                doSpaceBefore = false;
                            }
                       }
                       if (doSpaceBefore){
                          spaceBefore(previous, codeStyle.spaceBeforeCatch());
                       }
                       spaceAfterBefore(current, codeStyle.spaceBeforeCatchParen(), LPAREN);
                    }
                    break;
                }
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                {
                    braces.push(new StackEntry(ts));
                    break;
                }
                case DO: //("do", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    break;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeSwitchParen(), LPAREN);
                    }
                    break;
                }
                case DEFAULT: //("default", "keyword-directive"),
                case CASE: //("case", "keyword-directive"),
                {
                    braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                    break;
                }
                case BREAK: //("break", "keyword-directive"),
                {
                    break;
                }
                case CONTINUE: //("continue", "keyword-directive"),
                {
                    break;
                }
            }
            previous = current;
        }
        return diffs.getStorage();
    }
    
    /*package local*/ String getParentIndent(String prefix) {
        return getIndent(prefix, braces.getLength()-1);
    }

    /*package local*/ String getIndent(String prefix) {
        return getIndent(prefix, braces.getLength());
    }

    /*package local*/ String getIndent(String prefix, int shift) {
        shift = shift * codeStyle.getGlobalIndentSize();
        if (codeStyle.indentCasesFromSwitch()) {
            shift += codeStyle.getGlobalIndentSize() * braces.switchDepth();
        }
        StackEntry entry = braces.peek();
        if (entry != null) {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                switch (entry.getKind()){
                    case NAMESPACE: //("namespace", "keyword"), //C++
                    case CLASS: //("class", "keyword"), //C++
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                        break;
                    case IF: 
                    case ELSE: 
                    case FOR: 
                    case DO: 
                    case WHILE: 
                    case SWITCH: 
                    case CATCH: 
                        shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.getGlobalIndentSize();
                        break;
                    default:
                    {
                        if (entry.getKind() == LBRACE) {
                            if (entry.getImportantKind() != null &&
                                entry.getImportantKind() == ENUM) {
                                break;
                            }
                        }
                        if (entry.isLikeToArrayInitialization()){
                            break;
                        }
                        StatementKind kind = braces.getLastStatementKind(ts);
                        if (kind == null || 
                            !(kind == StatementKind.CLASS ||
                              kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                            shift += codeStyle.getFormatStatementContinuationIndent();
                        }
                        break;
                    }
                }
            }
        } else {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                StatementKind kind = braces.getLastStatementKind(ts);
                
                if (kind == null || 
                    !(kind == StatementKind.CLASS ||
                      kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                    shift += codeStyle.getFormatStatementContinuationIndent();
                }
            }
        }
        Token<CppTokenId> next = ts.lookNextImportant();
        if (next != null) {
            System.out.println("Indent:"+next.text().toString()+"="+shift);
        }
        if (shift > 0) {
            return spaces(prefix, shift);
        } else {
            return prefix;
        }
    }

    /*package local*/ String spaces(String prefix, int length) {
        StringBuilder spaces = new StringBuilder(prefix);
        for(int i = 0; i < length; i++){
            spaces.append(' ');
        }
        return spaces.toString();
    }

    private void braceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        StackEntry entry = braces.peek();
        if (entry != null && entry.getImportantKind() != null) {
            switch (entry.getImportantKind()) {
                case NAMESPACE: //("namespace", "keyword"), //C++
                {
                    // TODO divide for neamespace
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceNamespace(),
                            codeStyle.spaceBeforeClassDeclLeftBrace(), true);
                    return;
                }
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceClass(),
                            codeStyle.spaceBeforeClassDeclLeftBrace(), true);
                    return;
                }
                case IF: //("if", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeIfLeftBrace(), true);
                    return;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeElseLeftBrace(), true);
                    return;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeSwitchLeftBrace(), true);
                    return;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeWhileLeftBrace(), true);
                    return;
                }
                case DO: //("do", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeDoLeftBrace(), true);
                    return;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeForLeftBrace(), true);
                    return;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeTryLeftBrace(), true);
                    return;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeCatchLeftBrace(), true);
                    return;
                }
            }
        }
        if (entry != null && entry.isLikeToFunction()) {
            newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceDeclaration(),
                    codeStyle.spaceBeforeMethodDeclLeftBrace(), true);
        } else if (entry != null && entry.isLikeToArrayInitialization()) {
            Token<CppTokenId> p1 = ts.lookPreviousLineImportant();
            if (p1 != null && p1.id() == LBRACE) {
                // it a situation int a[][]={{
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                        codeStyle.spaceBeforeArrayInitLeftBrace(), true);
            } else {
                // TODO more control
                //newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                //        codeStyle.spaceBeforeArrayInitLeftBrace());
                boolean concurent = false;
                if (p1 != null) {
                    if (p1.id() == EQ){
                        concurent |= codeStyle.spaceAroundAssignOps();
                    }
                }
                spaceBefore(previous, concurent || codeStyle.spaceBeforeArrayInitLeftBrace());
            }
        } else {
            // TODO add options for block spaces 
            Token<CppTokenId> p1 = ts.lookPreviousImportant();
            if (p1 != null && p1.id() == LBRACE) {
                // it a situation while(true){{
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, true);
                return;
            }
            StackEntry prevEntry = braces.lookPerevious();
            if (prevEntry != null &&
                prevEntry.getImportantKind() != null && prevEntry.getImportantKind() == SWITCH){
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, true);
                return;
            }
            if (prevEntry == null ||
                prevEntry != null && prevEntry.getImportantKind() != null && prevEntry.getImportantKind() == NAMESPACE){
                // It is a K&R stryle of function definition
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, true);
                if (entry != null) {
                    entry.setLikeToFunction(true);
                }
                return;
            }
            newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(), true, true);
        }
    }

    private void formatElse(Token<CppTokenId> previous) {
        spaceBefore(previous, codeStyle.spaceBeforeElse());
        if (previous != null && ts.isFirstLineToken()) {
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                boolean done = false;
                if (diff.after != null) {
                    diff.after.replaceSpaces(getParentIndent("")); // NOI18N
                    done = true;
                }
                if (diff.replace != null && previous.id() == WHITESPACE) {
                    if (!done) {
                        diff.replace.replaceSpaces(getParentIndent("")); // NOI18N
                        done = true;
                    } else {
                        diff.replace.replaceSpaces(""); // NOI18N
                    }
                }
                if (diff.before != null && previous.id() == WHITESPACE){
                    if (!done) {
                        diff.before.replaceSpaces(getParentIndent("")); // NOI18N
                        done = true;
                    } else {
                        diff.before.replaceSpaces(""); // NOI18N
                    }
                }
                if (done) {
                    return;
                }
            }
            if (previous.id() == WHITESPACE) {
                Token<CppTokenId> p2 = ts.lookPrevious(2);
                if (p2 != null && p2.id()== NEW_LINE) {
                    ts.replacePrevious(previous, getParentIndent(""));
                } else {
                    ts.replacePrevious(previous, "");
                }
            } else if (previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE) {
                String text = getParentIndent(""); // NOI18N
                ts.addBeforeCurrent(text);
            }
        }
    }

    private void indentRbrace(StackEntry entry, Token<CppTokenId> previous,
                              int indent, Token<CppTokenId> current, boolean isClassDeclaration) {
        if (previous != null) {
            boolean done = false;
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.before != null && previous.id() == WHITESPACE) {
                    diff.before.replaceSpaces(getIndent("", indent)); // NOI18N
                    done = true;
                }
                if (diff.replace != null) {
                    if (!done) {
                        diff.replace.replaceSpaces(getIndent("", indent)); // NOI18N
                    } else {
                        diff.replace.replaceSpaces(""); // NOI18N
                    }
                    done = true;
                }
                if (diff.after != null) {
                    if (!done) {
                        if (diff.after.hasNewLine() || ts.isFirstLineToken()) {
                            diff.after.replaceSpaces(getIndent("", indent)); // NOI18N
                        } else {
                            diff.after.setText(getIndent("\n", indent)); // NOI18N
                        }
                    }
                    done = true;
                }
            }
            if (!done) {
                if (previous.id() == WHITESPACE) {
                    if (ts.isFirstLineToken()) {
                        ts.replacePrevious(previous, getIndent("", indent)); // NOI18N
                    } else {
                        ts.replacePrevious(previous, getIndent("\n", indent)); // NOI18N
                    }
                } else if (previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE) {
                    ts.addBeforeCurrent(getIndent("", indent)); // NOI18N
                }
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (isClassDeclaration) {
            if (next != null && !(next.id() == WHITESPACE || next.id() == NEW_LINE)) {
                ts.addAfterCurrent(current, " ");
            }
            return;
        }
        Token<CppTokenId> nextImportant = ts.lookNextImportant();
        if (nextImportant != null) {
            switch (nextImportant.id()) {
                case WHILE:
                {
                    if (entry != null && entry.getKind() == DO) {
                        if (!codeStyle.newLineWhile()) {
                            if (ts.isLastLineToken()) {
                                Token<CppTokenId> n2 = ts.lookNext(2);
                                if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                    ts.replaceNext(current, next, ""); // NOI18N
                                }
                            }
                        } else {
                            if (!ts.isLastLineToken()) {
                                ts.addAfterCurrent(current, getIndent("\n", indent)); // NOI18N
                            }
                        }
                        return;
                    }
                    break;
                }
                case CATCH:
                {
                    if (entry != null &&
                        (entry.getKind() == TRY || entry.getKind() == CATCH)) {
                        if (!codeStyle.newLineCatch()) {
                            if (ts.isLastLineToken()) {
                                Token<CppTokenId> n2 = ts.lookNext(2);
                                if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                    ts.replaceNext(current, next, ""); // NOI18N
                                }
                            }
                        } else {
                            if (!ts.isLastLineToken()) {
                                ts.addAfterCurrent(current, getIndent("\n", indent)); // NOI18N
                            }
                        }
                        return;
                    }
                    break;
                }
                case ELSE:
                {
                    if (!codeStyle.newLineElse()) {
                        if (ts.isLastLineToken()) {
                            Token<CppTokenId> n2 = ts.lookNext(2);
                            if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                ts.replaceNext(current, next, ""); // NOI18N
                            }
                        }
                    } else {
                        if (!ts.isLastLineToken()) {
                            ts.addAfterCurrent(current, getIndent("\n", indent)); // NOI18N
                        }
                    }
                    return;
                }
            }
        }
        next = ts.lookNextLineImportant();
        if (next != null && !(next.id() == COMMA || next.id() == SEMICOLON || next.id() == NEW_LINE)) {
            ts.addAfterCurrent(current, getIndent("\n", indent)); // NOI18N
        }
    }

    private void newLineFormat(Token<CppTokenId> previous, Token<CppTokenId> current, int parenDepth) {
        if (previous != null) {
            boolean done = false;
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.after != null) {
                    diff.after.replaceSpaces(""); // NOI18N
                    if (diff.replace != null){
                        diff.replace.replaceSpaces(""); // NOI18N
                    }
                    done = true;
                } else if (diff.replace != null) {
                    diff.replace.replaceSpaces(""); // NOI18N
                    done = true;
                }
            }
            if (!done && previous.id() == WHITESPACE) {
                ts.replacePrevious(previous, ""); // NOI18N
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (next.id() == NEW_LINE) {
                return;
            }
            String space = null;
            if (parenDepth > 0) {
                // get indent from left paren indent
                Token<CppTokenId> prev = ts.findOpenParenToken(parenDepth);
                if (prev != null) {
                    switch (prev.id()){
                        case IDENTIFIER:
                        {
                            if (braces.isDeclarationLevel()){
                                if (codeStyle.alignMultilineMethodParams()){
                                    int i = ts.openParenIndent(parenDepth);
                                    if (i >=0) {
                                        space = spaces("", i);
                                    }
                                }
                            } else {
                                if (codeStyle.alignMultilineCallArgs()){
                                    int i = ts.openParenIndent(parenDepth);
                                    if (i >=0) {
                                        space = spaces("", i);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (space == null) {
                Token<CppTokenId> first = ts.lookNextLineImportant();
                if (first != null && braces.getStatementContinuation()!=BracesStack.StatementContinuation.STOP) {
                    switch (first.id()) {
                        case CASE:
                        case DEFAULT:
                        case FOR:
                        case IF:
                        case ELSE:
                        case DO:
                        case WHILE:
                        case SWITCH:
                        case TRY:
                        case CATCH:
                        case BREAK:
                        case RETURN:
                        case CONTINUE:
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            braces.lastStatementStart = -1;
                    }
                }
                if (first != null && (first.id() == CASE ||first.id() == DEFAULT)){
                    space = getParentIndent(""); // NOI18N
                }
            }
            if (space == null) {
                space = getIndent(""); // NOI18N
            }
            if (next.id() == WHITESPACE) {
                ts.replaceNext(current, next, space);
            } else {
                if (space.length() > 0) {
                    ts.addAfterCurrent(current, space);
                }
            }
        }
    }

    // indent new line after preprocessor directive
    private void indentNewLine(Token<CppTokenId> current){
        if (current.id() == NEW_LINE) {
            return;
        }
        String space;
        Token<CppTokenId> first = ts.lookNextLineImportant();
        if (first != null && (first.id() == CASE ||first.id() == DEFAULT)){
            space = getParentIndent(""); // NOI18N
        } else {
            space = getIndent(""); // NOI18N
        }
        if (current.id() == WHITESPACE) {
            ts.replaceCurrent(current, space);
        } else {
            ts.addBeforeCurrent(space);
        }
    }

    private void reformatBlockComment(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (!ts.isFirstLineToken()){
            // do not format block comments inside cole line
            return;
        }
        int tab = codeStyle.getGlobalTabSize();
        if (tab <= 1) {
            tab = 4;
        }
        int originalIndent = 0;
        if (previous == null || previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE){
            originalIndent = 0;
        } else if (previous.id()==WHITESPACE) {
            CharSequence s = previous.text();
            for (int i = 0; i < previous.length(); i++) {
                if (s.charAt(i) == ' '){ // NOI18N
                    originalIndent++;
                } else if (s.charAt(i) == '\t'){ // NOI18N
                    originalIndent = (originalIndent/tab+1)*tab;
                }
            }
        }
        int requiredIndent = getIndent("").length(); // NOI18N
        int start = -1;
        int end = -1;
        int currentIndent = 0;
        CharSequence s = current.text();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') { // NOI18N
                start = i;
                end = i;
                currentIndent = 0;
            } else if (s.charAt(i) == ' ' || s.charAt(i) == '\t') { // NOI18N
                end = i;
                if (s.charAt(i) == ' '){ // NOI18N
                    currentIndent++;
                } else if (s.charAt(i) == '\t'){ // NOI18N
                    currentIndent = (currentIndent/tab+1)*tab;
                }
            } else {
                if (start >= 0) {
                    addCommentIndent(start, end, s.charAt(i), requiredIndent, originalIndent, currentIndent);
                }
                start = -1;
            }
        }
        addCommentIndent(start, end, '*', requiredIndent, originalIndent, currentIndent); // NOI18N
    }
    
    private void addCommentIndent(int start, int end, char c, int requiredIndent, int originalIndent, int currentIndent) {
        if (start >= 0 && end >= start) {
            if (c == '*') { // NOI18N
                diffs.addFirst(ts.offset() + start + 1, ts.offset() + end + 1, spaces(" ", requiredIndent));  // NOI18N 
            } else {
                int indent = requiredIndent + currentIndent - originalIndent;
                if (indent < 0) {
                    indent = requiredIndent;
                }
                diffs.addFirst(ts.offset() + start + 1, ts.offset() + end + 1, spaces("", indent)); 
            }
        }
    }

    private void whiteSpaceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null) {
            DiffResult diff = diffs.getDiffs(ts, 0);
            if (diff != null) {
                if (diff.replace != null) {
                    return;
                }
                if (diff.before != null){
                    ts.replaceCurrent(current, ""); // NOI18N
                    return;
                }
            }
            if (previous.id() == NEW_LINE ||
                previous.id() == PREPROCESSOR_DIRECTIVE) {
                // already formatted
                return;
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (next != null && next.id() == NEW_LINE) {
            // will be formatted on new line
            return;
        }
        if (previous == null) {
            ts.replaceCurrent(current, ""); // NOI18N
        } else {
            ts.replaceCurrent(current, " "); // NOI18N
        }
    }

    private void newLine(Token<CppTokenId> previous, Token<CppTokenId> current,
            CodeStyle.BracePlacement where, boolean spaceBefore, boolean newLineAfter){
        if (where == CodeStyle.BracePlacement.NEW_LINE) {
            newLineBefore();
        } else if (where == CodeStyle.BracePlacement.SAME_LINE) {
            if (ts.isFirstLineToken()){
                if (!tryRemoveLine(spaceBefore)){
                    newLineBefore();
                }
            } else {
                spaceBefore(previous, spaceBefore);
            }
        }
        if(newLineAfter && !ts.isLastLineToken()){
            ts.addAfterCurrent(current, getIndent("\n")); // NOI18N
        }
    }

    private void newLineBefore() {
        if (!ts.isFirstLineToken()) {
           Token<CppTokenId> previous = ts.lookPrevious();
           if (previous != null && previous.id() == WHITESPACE) {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null) {
                        diff.after.setText(getParentIndent("\n")); // NOI18N
                        if (diff.replace != null){
                            diff.replace.setText(""); // NOI18N
                        }
                        return;
                    } else if (diff.replace != null) {
                        diff.replace.setText(getParentIndent("\n")); // NOI18N
                        return;
                    }
                }
               ts.replacePrevious(previous, getParentIndent("\n")); // NOI18N\
           } else {
               ts.addBeforeCurrent(getParentIndent("\n")); // NOI18N\
           }
        } else {
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.after != null) {
                    diff.after.replaceSpaces(getParentIndent("")); // NOI18N
                    if (diff.replace != null){
                        diff.replace.replaceSpaces(""); // NOI18N
                    }
                    return;
                } else if (diff.replace != null) {
                    diff.replace.replaceSpaces(getParentIndent("")); // NOI18N
                    return;
                }
            }
            Token<CppTokenId> previous = ts.lookPrevious();
            if (previous != null) {
                if (previous.id() == WHITESPACE) {
                    ts.replacePrevious(previous, getParentIndent(""));
                } else if (previous.id() == NEW_LINE) {
                    String text = getParentIndent(""); // NOI18N
                    ts.addBeforeCurrent(text);
                }
            }
        }
    }

    private void spaceBefore(Token<CppTokenId> previous, boolean add){
        if (previous != null && !ts.isFirstLineToken()) {
            if (add) {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null && !diff.after.hasNewLine()) {
                        diff.after.replaceSpaces(" "); // NOI18N
                        if (diff.replace != null && !diff.replace.hasNewLine()){
                            diff.replace.replaceSpaces(""); // NOI18N
                        }
                        return;
                    } else if (diff.replace != null && !diff.replace.hasNewLine()) {
                        diff.replace.replaceSpaces(" "); // NOI18N
                        return;
                    }
                }
                if (!(previous.id() == WHITESPACE ||
                      previous.id() == NEW_LINE ||
                      previous.id() == PREPROCESSOR_DIRECTIVE)) {
                    ts.addBeforeCurrent(" "); // NOI18N
                }
            } else {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null && !diff.after.hasNewLine()) {
                        diff.after.replaceSpaces(""); // NOI18N
                        if (diff.replace != null && !diff.replace.hasNewLine()){
                            diff.replace.replaceSpaces(""); // NOI18N
                        }
                        return;
                    } else if (diff.replace != null && !diff.replace.hasNewLine()) {
                        diff.replace.replaceSpaces(""); // NOI18N
                        return;
                    }
                }
                if (previous.id() == WHITESPACE && !ts.isFirstLineToken()) {
                    Token<CppTokenId> p2 = ts.lookPrevious(2);
                    if (p2 == null || !OPERATOR_CATEGORY.equals(p2.id().primaryCategory())){
                        ts.replacePrevious(previous, ""); // NOI18N
                    }
                }
            }
        }
    }

    private void spaceAfter(Token<CppTokenId> current, boolean add){
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (add) {
                if (!(next.id() == WHITESPACE ||
                      next.id() == NEW_LINE)) {
                    ts.addAfterCurrent(current, " "); // NOI18N
                }
            } else {
                if (next.id() == WHITESPACE) {
                    Token<CppTokenId> n2 = ts.lookNext(2);
                    if (n2 == null || !OPERATOR_CATEGORY.equals(n2.id().primaryCategory())){
                        ts.replaceNext(current, next, ""); // NOI18N
                    }
                }
            }
        }
    }

    private void spaceAfterBefore(Token<CppTokenId> current, boolean add, CppTokenId before){
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (next.id() == WHITESPACE) {
                Token<CppTokenId> p = ts.lookNext(2);
                if (p!=null && p.id()==before) {
                    if (!add) {
                        ts.replaceNext(current, next, ""); // NOI18N
                    }
                }
            } else if (next.id() == before) {
                if (add) {
                    ts.addAfterCurrent(current, " "); // NOI18N
                }
            }
        }
    }

    private void formatLeftParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = ts.lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                        spaceAfter(current, codeStyle.spaceWithinIfParens());
                        return;
                    case FOR:
                        spaceAfter(current, codeStyle.spaceWithinForParens());
                        return;
                    case WHILE:
                        spaceAfter(current, codeStyle.spaceWithinWhileParens());
                        return;
                    case SWITCH:
                        spaceAfter(current, codeStyle.spaceWithinSwitchParens());
                        return;
                    case CATCH:
                        spaceAfter(current, codeStyle.spaceWithinCatchParens());
                        return;
                }
            }
            if (isTypeCast()){
                spaceAfter(current, codeStyle.spaceWithinTypeCastParens());
                return;
            }
            p = ts.lookPreviousImportant();
            if (p != null && p.id() == IDENTIFIER) {
                StackEntry entry = braces.peek();
                if (entry == null){
                    spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen());
                    return;
                }
                if (entry.getImportantKind() != null) {
                    switch (entry.getImportantKind()) {
                        case CLASS:
                        case NAMESPACE:
                            spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen());
                            return;
                    }
                }
                spaceBefore(previous, codeStyle.spaceBeforeMethodCallParen());
                return;
            }
        }
    }

    private void formatRightParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = ts.lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                        spaceBefore(previous, codeStyle.spaceWithinIfParens());
                        return;
                    case FOR:
                        spaceBefore(previous, codeStyle.spaceWithinForParens());
                        return;
                    case WHILE:
                        spaceBefore(previous, codeStyle.spaceWithinWhileParens());
                        return;
                    case SWITCH:
                        spaceBefore(previous, codeStyle.spaceWithinSwitchParens());
                        return;
                    case CATCH:
                        spaceBefore(previous, codeStyle.spaceWithinCatchParens());
                        return;
                }
            }
            if (isTypeCast()){
                spaceBefore(previous, codeStyle.spaceWithinTypeCastParens());
                spaceAfter(current, codeStyle.spaceAfterTypeCast());
            }
        }
    }

    private boolean isTypeCast() {
        int index = ts.index();
        try {
            boolean findId = false;
            if (ts.token().id() == RPAREN) {
                while (ts.movePrevious()) {
                    switch (ts.token().id()) {
                        case LPAREN:
                        {
                            if (findId) {
                                ts.moveIndex(index);
                                ts.moveNext();
                                Token<CppTokenId> next = ts.lookNextImportant();
                                return next != null && next.id() == IDENTIFIER;
                            }
                            return false;
                        }
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            findId = true;
                            break;
                        case IDENTIFIER:
                            if (findId) {
                                return false;
                            }
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                        case WHITESPACE:
                        case NEW_LINE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case DOXYGEN_COMMENT:
                        case PREPROCESSOR_DIRECTIVE:
                            break;
                        default:
                            return false;
                    }
                }
            } else if (ts.token().id() == LPAREN) {
                while (ts.moveNext()) {
                    switch (ts.token().id()) {
                        case RPAREN:
                        {
                            if (findId) {
                                Token<CppTokenId> next = ts.lookNextImportant();
                                return next != null && next.id() == IDENTIFIER;
                            }
                            return false;
                        }
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            findId = true;
                            break;
                        case IDENTIFIER:
                            if (findId) {
                                return false;
                            }
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                        case WHITESPACE:
                        case NEW_LINE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case DOXYGEN_COMMENT:
                        case PREPROCESSOR_DIRECTIVE:
                            break;
                        default:
                            return false;
                    }
                }
            }
            return false;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    private boolean tryRemoveLine(boolean addSpace){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return false;
                }
                if (ts.token().id() == NEW_LINE){
                    if (ts.movePrevious()) {
                        if (ts.token().id() == WHITESPACE) {
                            ts.movePrevious();
                            replaceSegment(addSpace, index);
                            return true;
                        } else if (ts.token().id() != LINE_COMMENT) {
                            replaceSegment(addSpace, index);
                            return true;
                        }
                    }
                    return false;
                } else if (ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    return false;
                } else if (ts.token().id() != WHITESPACE){
                    replaceSegment(addSpace, index);
                    return true;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    // <importantFrom><WS><NL><WS><importantTo>
    // current ts point to importantFrom
    // indexTo point to importantTo
    // method removes chain <WS><NL><WS> or replaces it to on space
    private void replaceSegment(boolean addSpace, int indexTo) {
        boolean first = true;
        Diff diffToSpace = null;
        while (ts.index() < indexTo) {
            DiffResult diff = diffs.getDiffs(ts, 0);
            if (diff != null) {
                if (!first) {
                    if (diff.replace != null) {
                        if (diffToSpace == null) {
                            diffToSpace = diff.replace;
                        }
                        diff.replace.setText(""); // NOI18N
                    } else {
                        Diff added = diffs.addFirst(ts.offset(), ts.offset()+ts.token().length(), ""); // NOI18N
                        if (diffToSpace == null) {
                            diffToSpace = added;
                        }
                    }
                }
                if (diff.after != null) {
                    if (diffToSpace == null) {
                        diffToSpace = diff.after;
                    }
                    diff.after.setText(""); // NOI18N
                }
            }
            if (!first && diff == null) {
                Diff added = diffs.addFirst(ts.offset(), ts.offset() + ts.token().length(), ""); // NOI18N
                if (diffToSpace == null) {
                    diffToSpace = added;
                }
            }
            first = false;
            ts.moveNext();
        }
        if (diffToSpace != null && addSpace){
            diffToSpace.setText(" "); // NOI18N
        }
    }
    
    
    private OperatorKind getOperatorKind(Token<CppTokenId> current){
        Token<CppTokenId> previous = ts.lookPreviousImportant();
        Token<CppTokenId> next = ts.lookNextImportant();
        if (previous != null && next != null) {
            String prevCategory = previous.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(prevCategory) ||
                (SEPARATOR_CATEGORY.equals(prevCategory) &&
                 previous.id() != RPAREN && previous.id() != RBRACKET)){
                switch(current.id()){
                    case STAR:
                    case AMP:
                        return OperatorKind.TYPE_MODIFIER;
                    case PLUS:
                    case MINUS:
                    case GT:
                    case LT:
                    default:
                        return OperatorKind.SEPARATOR;
                }
            }
            if (NUMBER_CATEGORY.equals(prevCategory) ||
                LITERAL_CATEGORY.equals(prevCategory) ||
                CHAR_CATEGORY.equals(prevCategory) ||
                STRING_CATEGORY.equals(prevCategory)){
                return OperatorKind.BINARY;
            }
            String nextCategory = next.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(nextCategory)){
                switch(current.id()){
                    case STAR:
                    case AMP:
                        return OperatorKind.TYPE_MODIFIER;
                    case PLUS:
                    case MINUS:
                    case GT:
                    case LT:
                    default:
                        return OperatorKind.SEPARATOR;
                }
            }
            if (NUMBER_CATEGORY.equals(nextCategory) ||
                LITERAL_CATEGORY.equals(nextCategory) ||
                CHAR_CATEGORY.equals(nextCategory) ||
                STRING_CATEGORY.equals(nextCategory)){
                return OperatorKind.BINARY;
            }
            if (previous.id() == IDENTIFIER){
                if (next.id() == LPAREN){
                    // TODO need detect that previous ID is not type
                    if (braces.isDeclarationLevel()){
                        switch(current.id()){
                            case STAR:
                            case AMP:
                                return OperatorKind.TYPE_MODIFIER;
                            case PLUS:
                            case MINUS:
                            case GT:
                            case LT:
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    }
                    return OperatorKind.BINARY;
                }
                if (OPERATOR_CATEGORY.equals(nextCategory) ||
                    SEPARATOR_CATEGORY.equals(nextCategory)){
                    switch(current.id()){
                        case STAR:
                        case AMP:
                            return OperatorKind.TYPE_MODIFIER;
                        case PLUS:
                        case MINUS:
                        case GT:
                        case LT:
                        default:
                            return OperatorKind.SEPARATOR;
                    }
                }
                if (next.id() == IDENTIFIER) {
                    // TODO need detect that previous ID is not type
                    if (braces.isDeclarationLevel()){
                        switch(current.id()){
                            case STAR:
                            case AMP:
                                return OperatorKind.TYPE_MODIFIER;
                            case PLUS:
                            case MINUS:
                            case GT:
                            case LT:
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    }
                    if (isLikeExpession()){
                        return OperatorKind.BINARY;
                    }
                }
            }
        }
        return OperatorKind.SEPARATOR;
    }
    
    private boolean isLikeExpession(){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                    case IDENTIFIER:
                        break;
                    case COMMA:
                    case SEMICOLON:
                    case EQ:
                        return false;
                    default:
                        return true;
                }
            }
            return true;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    /*package local*/ boolean doFormat(){
        return ts.offset() >= this.startOffset;
    }
    
    private static enum OperatorKind {
        BINARY,
        UNARY,
        SEPARATOR,
        TYPE_MODIFIER
    }
}
