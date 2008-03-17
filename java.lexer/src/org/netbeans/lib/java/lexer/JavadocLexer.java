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

package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for javadoc language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavadocLexer implements Lexer<JavadocTokenId> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<JavadocTokenId> tokenFactory;
    
    public JavadocLexer(LexerRestartInfo<JavadocTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }
    
    public Object state() {
        return null;
    }
    
    public Token<JavadocTokenId> nextToken() {
        int ch = input.read();
        
        if (ch == EOF) {
            return null;
        }
        
        if (Character.isJavaIdentifierStart(ch)) {
            //TODO: EOF
            while (Character.isJavaIdentifierPart(input.read()))
                ;
            
            input.backup(1);
            return token(JavadocTokenId.IDENT);
        }
        
        if ("@<.#".indexOf(ch) == (-1)) {
            //TODO: EOF
            ch = input.read();
            
            while (!Character.isJavaIdentifierStart(ch) && "@<.#".indexOf(ch) == (-1) && ch != EOF)
                ch = input.read();
            
            if (ch != EOF)
                input.backup(1);
            return token(JavadocTokenId.OTHER_TEXT);
        }
        
        switch (ch) {
            case '@':
                while (true) {
                    ch = input.read();
                    
                    if (!Character.isLetter(ch)) {
                        input.backup(1);
                        return tokenFactory.createToken(JavadocTokenId.TAG, input.readLength());
                    }
                }
            case '<':
                int backupCounter = 0;
                boolean newline = false;
                boolean asterisk = false;
                while (true) {
                    ch = input.read();
                    ++backupCounter;
                    if (ch == '>' || ch == EOF) {
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '<') {
                        input.backup(1);
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '\n') {
                        backupCounter = 1;
                        newline = true;
                        asterisk = false;
                    } else if (newline && ch == '@') {
                        input.backup(backupCounter);
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (newline && !asterisk && ch == '*') {
                        asterisk = true;
                    } else if (newline && !Character.isWhitespace(ch)) {
                        newline = false;
                    }
                }
            case '.':
                return token(JavadocTokenId.DOT);
            case '#':
                return token(JavadocTokenId.HASH);
        } // end of switch (ch)
        
        assert false;
        
        return null;
    }

    private Token<JavadocTokenId> token(JavadocTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

}
