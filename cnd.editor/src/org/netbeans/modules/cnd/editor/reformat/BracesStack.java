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

import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 * @author Alexander Simon
 */
class BracesStack {

    private Stack<StackEntry> stack = new Stack<StackEntry>();

    BracesStack() {
        super();
    }

    public void push(StackEntry entry) {
        if (entry.getKind() == ELSE){
            if (stack.size() > 0 && stack.peek().getKind() == IF) {
                stack.pop();
            }
        }
        stack.push(entry);
        System.out.println("push: "+toString());
    }

    private StackEntry safePop() {
        if (stack.empty()) {
            return null;
        }
        return stack.pop();
    }

    public StackEntry pop(TokenSequence<CppTokenId> ts) {
        StackEntry res = popImpl(ts);
        System.out.println("pop "+ts.token().id().name()+": "+toString());
        return res;
    }
    
    public StackEntry popImpl(TokenSequence<CppTokenId> ts) {
        if (stack.empty()) {
            return null;
        }
        CppTokenId id = ts.token().id();
        int brace;
        if (id == RBRACE) {
            brace = 0;
            for (int i = stack.size()-1; i >= 0; i--){
                StackEntry top = stack.get(i);
                if (top.getKind() == LBRACE){
                    brace = i-1;
                    break;
                }
            }
            if (brace < 0){
                StackEntry top = stack.get(0);
                stack.setSize(0);
                return top;
            }
        } else {
            brace = stack.size() - 1;
        }
        Token<CppTokenId> next = getNextImportant(ts);
        for (int i = brace; i >= 0; i--) {
            StackEntry top = stack.get(i);
            switch (top.getKind()) {
                case LBRACE: {
                    stack.setSize(i + 1);
                    return top;
                }
                case IF: //("if", "keyword-directive"),
                {
                    if (next != null && next.id() == ELSE) {
                        stack.setSize(i + 1);
                        return top;
                    }
                }
                case ELSE: //("else", "keyword-directive"),
                    break;
                case TRY: //("try", "keyword-directive"), // C++
                case CATCH: //("catch", "keyword-directive"), //C++
                case SWITCH: //("switch", "keyword-directive"),
                case FOR: //("for", "keyword-directive"),
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                case DO: //("do", "keyword-directive"),
                case WHILE: //("while", "keyword-directive"),
                    break;
            }
        }
        return null;
    }

    
    public StackEntry peek() {
        if (stack.empty()) {
            return null;
        }
        return stack.peek();
    }

    public int getLength() {
        StackEntry prev = null;
        int res = 0;
        for(int i = 0; i < stack.size(); i++){
            StackEntry entry = stack.get(i);
            if (entry.getKind() == LBRACE) {
                if (prev == null || prev.getKind()==LBRACE) {
                    res++;
                }
            } else {
                res++;
            }
            prev = entry;
        }
        return res;
    }
    

    private Token<CppTokenId> getNextImportant(TokenSequence<CppTokenId> ts) {
        int i = ts.index();
        try {
            while (true) {
                if (!ts.moveNext()) {
                    return null;
                }
                Token<CppTokenId> current = ts.token();
                switch (current.id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case BLOCK_COMMENT:
                    case LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    case IF: //("if", "keyword-directive"),
                    case ELSE: //("else", "keyword-directive"),
                    case SWITCH: //("switch", "keyword-directive"),
                    case ASM: //("asm", "keyword-directive"), // gcc and C++
                    case WHILE: //("while", "keyword-directive"),
                    case DO: //("do", "keyword-directive"),
                    case FOR: //("for", "keyword-directive"),
                    case TRY: //("try", "keyword-directive"), // C++
                    case CATCH: //("catch", "keyword-directive"), //C++
                        return current;
                    default:
                        return null;
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < stack.size(); i++){
            StackEntry entry = stack.get(i);
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(entry.toString());
        }
        buf.append("+"+getLength());
        return buf.toString();
    }
}
