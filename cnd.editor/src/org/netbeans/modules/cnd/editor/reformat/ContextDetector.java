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

package org.netbeans.modules.cnd.editor.reformat;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 * @author Alexander Simon
 */
public class ContextDetector extends ExtendedTokenSequence {
    private BracesStack braces;
    /*package local*/ ContextDetector(TokenSequence<CppTokenId> ts, DiffLinkedList diffs, BracesStack braces){
        super(ts, diffs);
        this.braces = braces;
    }

    /*package local*/ boolean isLikeTemplate(Token<CppTokenId> current){
        int index = index();
        try {
            boolean back = current.id() == GT;
            Token<CppTokenId> head = null;
            if (!back){
                head = lookPreviousImportant();
                if (head == null || head.id() != IDENTIFIER){
                    return false;
                }
            }
            int depth = 0;
            while(true) {
                if (back) {
                    if (!movePrevious()) {
                        return false;
                    }
                } else {
                    if (!moveNext()){
                        return false;
                    }
                }
                switch (token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    case GT:
                        if (back) {
                            depth++;
                        } else {
                            if (depth == 0) {
                                // end of template
                                return true;
                            } else
                            depth--;
                        }
                        break;
                    case LT:
                        if (back) {
                            if (depth == 0) {
                                // start of template
                                head = lookPreviousImportant();
                                if (head == null || head.id() != IDENTIFIER){
                                    return false;
                                }
                                return true;
                            } else
                            depth--;
                        } else {
                            depth++;
                        }
                        break;
                    case FALSE:
                    case TRUE:
                    case INT_LITERAL:
                    case LONG_LITERAL:
                    case FLOAT_LITERAL:
                    case DOUBLE_LITERAL:
                    case UNSIGNED_LITERAL:
                    case CHAR_LITERAL:
                    case STRING_LITERAL:
                        //it's a template specialization
                        break;
                    case SCOPE:
                    case STRUCT:
                    case CONST:
                    case VOID:
                    case UNSIGNED:
                    case CHAR:
                    case SHORT:
                    case INT:
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                    case AMP:
                    case STAR:
                    case COMMA:
                    case IDENTIFIER:
                        break;
                    default:
                        return false;
                }
            }
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    /*package local*/ boolean isTypeCast() {
        int index = index();
        try {
            boolean findId = false;
            boolean findModifier = false;
            if (token().id() == RPAREN) {
                while (movePrevious()) {
                    switch (token().id()) {
                        case LPAREN:
                        {
                            if (findId) {
                                moveIndex(index);
                                moveNext();
                                return checknextAfterCast();
                            }
                            return false;
                        }
                        case STRUCT:
                        case CONST:
                        case VOID:
                        case UNSIGNED:
                        case CHAR:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            return true;
                        case IDENTIFIER:
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                            if (findId) {
                                return false;
                            }
                            findModifier = true;
                            break;
                        case WHITESPACE:
                        case ESCAPED_WHITESPACE:
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
            } else if (token().id() == LPAREN) {
                while (moveNext()) {
                    switch (token().id()) {
                        case RPAREN:
                        {
                            if (findId) {
                                return checknextAfterCast();
                            }
                            return false;
                        }
                        case CONST:
                        case STRUCT:
                        case VOID:
                        case UNSIGNED:
                        case CHAR:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            return true;
                        case IDENTIFIER:
                            if (findModifier) {
                                return false;
                            }
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                            findModifier = true;
                            break;
                        case WHITESPACE:
                        case ESCAPED_WHITESPACE:
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
            moveIndex(index);
            moveNext();
        }
    }

    private boolean checknextAfterCast() {
        Token<CppTokenId> next = lookNextImportant();
        if (next != null) {
            String prevCategory = next.id().primaryCategory();
            if (NUMBER_CATEGORY.equals(prevCategory) ||
                    LITERAL_CATEGORY.equals(prevCategory) ||
                    CHAR_CATEGORY.equals(prevCategory) ||
                    STRING_CATEGORY.equals(prevCategory)) {
                return true;
            }
            switch (next.id()) {
                case IDENTIFIER:
                case TILDE:
                case LPAREN:
                    return true;
            }
        }
        return false;
    }
    
    /*package local*/ OperatorKind getOperatorKind(Token<CppTokenId> current){
        Token<CppTokenId> previous = lookPreviousImportant();
        Token<CppTokenId> next = lookNextImportant();
        if (previous != null && next != null) {
            String prevCategory = previous.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(prevCategory) ||
                KEYWORD_DIRECTIVE_CATEGORY.equals(prevCategory) ||
                (SEPARATOR_CATEGORY.equals(prevCategory) &&
                 previous.id() != RPAREN && previous.id() != RBRACKET)){
                switch(current.id()){
                    case STAR:
                    case AMP:
                        return OperatorKind.TYPE_MODIFIER;
                    case PLUS:
                    case MINUS:
                        return OperatorKind.UNARY;
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
                        return OperatorKind.BINARY;
                    case PLUS:
                    case MINUS:
                        return OperatorKind.BINARY;
                    case GT:
                    case LT:
                    default:
                        return OperatorKind.SEPARATOR;
                }
            }
            if (NUMBER_CATEGORY.equals(nextCategory) ||
                LITERAL_CATEGORY.equals(nextCategory) ||
                CHAR_CATEGORY.equals(nextCategory) ||
                STRING_CATEGORY.equals(nextCategory))  {
                if (SEPARATOR_CATEGORY.equals(prevCategory)||
                    OPERATOR_CATEGORY.equals(prevCategory)) {
                     switch(current.id()){
                        case STAR:
                        case AMP:
                            return OperatorKind.TYPE_MODIFIER;
                        case GT:
                        case LT:
                            return OperatorKind.BINARY;
                        case PLUS:
                        case MINUS:
                        default:
                            switch (previous.id()) {
                                case RPAREN://)")", "separator"),
                                case RBRACKET://("]", "separator"),
                                    return OperatorKind.BINARY;
                            }
                            return OperatorKind.UNARY;
                    }
                } else {
                    return OperatorKind.BINARY;
                }
            }
            if (previous.id() == RPAREN || previous.id() == RBRACKET){
                if (next.id() == IDENTIFIER) {
                    if (isPreviousStatementParen()) {
                        switch(current.id()){
                            case STAR:
                            case AMP:
                                return OperatorKind.TYPE_MODIFIER;
                            case PLUS:
                            case MINUS:
                                return OperatorKind.UNARY;
                            case GT:
                            case LT:
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    } else {
                        return OperatorKind.BINARY;
                    }
                }
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
                                return OperatorKind.BINARY;
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
                            return OperatorKind.BINARY;
                        case GT:
                        case LT:
                        default:
                            return OperatorKind.SEPARATOR;
                    }
                }
                if (next.id() == IDENTIFIER) {
                    // TODO need detect that previous ID is not type
                    switch(current.id()){
                        case STAR:
                        case AMP:
                            if (braces.isDeclarationLevel()) {
                                return OperatorKind.TYPE_MODIFIER;
                            } else if (isLikeForDeclaration()) {
                                return OperatorKind.TYPE_MODIFIER;
                            } else if (isLikeExpession()) {
                                return OperatorKind.BINARY;
                            }
                            return OperatorKind.SEPARATOR;
                        case PLUS:
                        case MINUS:
                            return OperatorKind.BINARY;
                        case GT:
                        case LT:
                        default:
                            if (braces.isDeclarationLevel()) {
                                return OperatorKind.SEPARATOR;
                            } else if (isLikeTemplate(current)) {
                                return OperatorKind.SEPARATOR;
                            } else if (isLikeExpession()) {
                                return OperatorKind.BINARY;
                            }
                            return OperatorKind.SEPARATOR;
                    }
                }
            }
        }
        return OperatorKind.SEPARATOR;
    }
    
    private boolean isPreviousStatementParen(){
        int index = index();
        try {
            while(movePrevious()){
                switch (token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        return isStatementParen();
                }
            }
            return true;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    private boolean isLikeForDeclaration(){
        StackEntry entry = braces.peek();
        if (entry == null || entry.getKind() != FOR){
            return false;
        }
        int index = index();
        int level = 1;
        try {
            while (movePrevious()) {
                switch (token().id()) {
                    case RPAREN:
                        level++;
                        break;
                    case LPAREN:
                        level--;
                        break;
                    case FOR:
                        if (level == 0) {
                            return true;
                        }
                        return false;
                    case SEMICOLON:
                    case EQ:
                        return false;
                    default:
                        break;
                }
            }
            return false;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    private boolean isStatementParen(){
        if (token().id() == RPAREN){
            int level = 1;
            while(movePrevious()){
                switch (token().id()) {
                    case RPAREN:
                        level++;
                        break;
                    case LPAREN:
                        level--;
                        if (level == 0){
                            Token<CppTokenId> previous = lookPreviousImportant();
                            if (previous != null) {
                                switch (previous.id()) {
                                    case FOR:
                                    case IF:
                                    case WHILE:
                                    case CATCH:
                                    case SWITCH:
                                        return true;
                                    default:
                                        while(movePrevious()){
                                            switch (token().id()) {
                                                case WHITESPACE:
                                                case ESCAPED_WHITESPACE:
                                                case NEW_LINE:
                                                case LINE_COMMENT:
                                                case BLOCK_COMMENT:
                                                case DOXYGEN_COMMENT:
                                                case PREPROCESSOR_DIRECTIVE:
                                                    break;
                                                default:
                                                    return index() == braces.lastStatementStart;
                                            }
                                        }
                                        break;
                                }
                            }
                            return false;
                        }
                        break;
                }
            }
        }
        return false;
    }
    
    private boolean isLikeExpession(){
        StackEntry entry = braces.peek();
        if (entry != null && 
           (entry.getKind() == FOR || entry.getKind() == WHILE || entry.getKind() == IF)){
            return true;
        }
        int index = index();
        try {
            while(moveNext()){
                switch (token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
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
            moveIndex(index);
            moveNext();
        }
    }
    
    /*package local*/ static enum OperatorKind {
        BINARY,
        UNARY,
        SEPARATOR,
        TYPE_MODIFIER
    }
}
