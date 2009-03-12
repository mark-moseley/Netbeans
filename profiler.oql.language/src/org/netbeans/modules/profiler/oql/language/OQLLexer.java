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

package org.netbeans.modules.profiler.oql.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 *
 * @author Jaroslav Bachorik
 */
class OQLLexer implements Lexer<OQLTokenId> {
    private static final String TOKEN_FROM = "FROM"; // NOI18N
    private static final String TOKEN_INSTANCEOF = "INSTANCEOF"; // NOI18N
    private static final String TOKEN_SELECT = "SELECT"; // NOI18N
    private static final String TOKEN_WHERE = "WHERE"; // NOI18N

    enum State {
        INIT,
        IN_SELECT,
        IN_FROM,
        IN_WHERE,
        IN_CLASSNAME,
        PLAIN_JS,
        FROM,
        FROM_INSTANCEOF,
        CLASS_ALIAS,
        JSBLOCK,
        ERROR
    };

    private LexerInput          input;
    private TokenFactory<OQLTokenId>
                                tokenFactory;
    private State               state = State.INIT;
    final private Pattern       classPattern = Pattern.compile("(\\[*)[a-z]+(?:[a-z 0-9]*)(?:[\\. \\$][a-z 0-9]+)*(\\[\\])*", Pattern.CASE_INSENSITIVE); // NOI18N


    OQLLexer (LexerRestartInfo<OQLTokenId> info) {
        input = info.input ();
        tokenFactory = info.tokenFactory ();
        if (info.state () != null)
            state = (State) info.state ();
    }

    public Token<OQLTokenId> nextToken () {
        for (;;) {
            int actChar = input.read();
            if (actChar == LexerInput.EOF) {
                break;
            }
            switch (state) {
                case INIT: {
                    String lastToken = input.readText().toString().toUpperCase();
                    if (Character.isWhitespace(actChar)) {
                        return tokenFactory.createToken(OQLTokenId.WHITESPACE);
                    } else {
                        input.backup(lastToken.length());
                        if (TOKEN_SELECT.startsWith(lastToken.trim())) {
                            state = State.IN_SELECT;
                        } else {
                            state = State.PLAIN_JS;
                        }
                    }
                    break;
                }
                case IN_SELECT: {
                    String lastToken = input.readText().toString().toUpperCase();
                    String trimmed = lastToken.trim();

                    if (Character.isWhitespace(actChar)) {
                        if (trimmed.length() == 0) return tokenFactory.createToken(OQLTokenId.WHITESPACE);
                        if (TOKEN_SELECT.equals(trimmed)) {
                            state = State.JSBLOCK;
                            return tokenFactory.createToken(OQLTokenId.KEYWORD);
                        } else {
                            state = State.ERROR;
                            input.backup(lastToken.length());
                        }
                    } else {
                        if (!TOKEN_SELECT.startsWith(trimmed)) {
                            input.backup(lastToken.length());
                            state = State.PLAIN_JS;
                        }
                    }
                    break;
                }

                case IN_FROM: {
                    if (Character.isWhitespace(actChar)) {
                        String lastToken = input.readText().toString().toUpperCase();
                        if (lastToken.trim().length() == 0) return tokenFactory.createToken(OQLTokenId.WHITESPACE);
                        if (TOKEN_FROM.equals(lastToken.trim())) {
                            state = State.FROM;
                            return tokenFactory.createToken(OQLTokenId.KEYWORD, lastToken.length(), PartType.COMPLETE);
                        } else {
                            state = State.ERROR;
                            input.backup(lastToken.length());
                        }
                    }
                    break;
                }

                case FROM: {
                    String lastToken = input.readText().toString().toUpperCase();
                    if (TOKEN_INSTANCEOF.startsWith(lastToken.trim())) {
                        state = State.FROM_INSTANCEOF;
                        input.backup(lastToken.length());
                    } else {
                        state = State.IN_CLASSNAME;
                    }
                    break;
                }

                case FROM_INSTANCEOF: {
                    String lastToken = input.readText().toString().toUpperCase();
                    String trimmed = lastToken.trim();
                    if (!TOKEN_INSTANCEOF.startsWith(trimmed)) {
                        state = State.IN_CLASSNAME;
                        input.backup(lastToken.length());
                    }
                    if (Character.isWhitespace(actChar)) {
                        if (trimmed.length() == 0) return tokenFactory.createToken(OQLTokenId.WHITESPACE);
                        if (TOKEN_INSTANCEOF.equals(trimmed)) {
                            state = State.IN_CLASSNAME;
                            return tokenFactory.createToken(OQLTokenId.KEYWORD);
                        } else {
                            state = State.ERROR;
                            input.backup(lastToken.length());
                        }
                    }
                    break;
                }

                case JSBLOCK: {
                    if (Character.isWhitespace(actChar)) {
                        String lastToken = input.readText().toString().toUpperCase();
                        String trimmed = lastToken.trim();
                        if (trimmed.endsWith(TOKEN_FROM)) {
                            state = State.IN_FROM;
                            input.backup(5);
                            return tokenFactory.createToken(OQLTokenId.JSBLOCK, lastToken.length() - 5);
                        } else if (TOKEN_SELECT.equals(trimmed) || TOKEN_INSTANCEOF.equals(trimmed) || TOKEN_WHERE.equals(trimmed)) {
                            state = State.ERROR;
                            input.backup(lastToken.length());
                        }
                    }
                    break;
                }

                case IN_CLASSNAME: {
                    if (Character.isWhitespace(actChar)) {
                        String lastToken = input.readText().toString().toUpperCase();
                        Matcher matcher = classPattern.matcher(lastToken.trim());
                        if (matcher.matches()) {
                            if ((isEmpty(matcher.group(1)) ? 0 : 1) + (isEmpty(matcher.group(2)) ? 0 : 1) > 1) {
                                state = State.ERROR;
                                input.backup(lastToken.length());
                            }
                            state = State.CLASS_ALIAS;
                            return tokenFactory.createToken(OQLTokenId.CLAZZ);
                        }
                    }
                    break;
                }

                case CLASS_ALIAS: {
                    String lastToken = input.readText().toString().toUpperCase();

                    if (TOKEN_SELECT.equals(lastToken) ||
                        TOKEN_FROM.equals(lastToken) ||
                        TOKEN_INSTANCEOF.equals(lastToken) ||
                        TOKEN_WHERE.equals(lastToken)) {
                        state = State.ERROR;
                        input.backup(lastToken.length());
                        break;
                    }
                    if (Character.isWhitespace(actChar)) {
                        if (lastToken.trim().length() == 0) return tokenFactory.createToken(OQLTokenId.WHITESPACE);
                        state = State.IN_WHERE;
                        return tokenFactory.createToken(OQLTokenId.IDENTIFIER);
                    }
                    if (!Character.isLetter(actChar)) {
                        state = State.ERROR;
                        input.backup(1);
                        break;
                    }
                    break;
                }

                case IN_WHERE: {
                    String lastToken = input.readText().toString().toUpperCase();
                    String trimmed = lastToken.trim();

                    if (!TOKEN_WHERE.startsWith(trimmed)) {
                        state = State.ERROR;
                        input.backup(lastToken.length());
                    }
                    if (Character.isWhitespace(actChar)) {
                        if (trimmed.length() == 0) return tokenFactory.createToken(OQLTokenId.WHITESPACE);
                        if (TOKEN_WHERE.equals(trimmed)) {
                            state = State.JSBLOCK;
                            return tokenFactory.createToken(OQLTokenId.KEYWORD);
                        } else {
                            state = State.ERROR;
                            input.backup(lastToken.length());
                        }
                    }
                    break;
                }

                case PLAIN_JS: {
                    break;
                }
                case ERROR: {
                    return tokenFactory.createToken(OQLTokenId.ERROR);
                }
            } // switch (state)
        }

        if (input.readLength() == 0) return null;
        switch (state) {
            case INIT: {
                return tokenFactory.createToken(OQLTokenId.UNKNOWN);
            }
            case IN_SELECT: {
                return tokenFactory.createToken(OQLTokenId.KEYWORD, input.readLength(), PartType.START);
            }
            case JSBLOCK: {
                String lastToken = input.readText().toString().trim().toUpperCase();
                if (lastToken.endsWith(TOKEN_FROM)) {
                    state = State.IN_FROM;
                    input.backup(5);
                    return tokenFactory.createToken(OQLTokenId.JSBLOCK, lastToken.length() - 5);
                } else {
                    return tokenFactory.createToken(OQLTokenId.JSBLOCK, input.readLength(), PartType.START);
                }
            }
            case PLAIN_JS: {
                return tokenFactory.createToken(OQLTokenId.JSBLOCK);
            }
            case IN_FROM: {
                return tokenFactory.createToken(OQLTokenId.KEYWORD, input.readLength(), PartType.START);
            }
            case FROM: {
                return tokenFactory.createToken(OQLTokenId.UNKNOWN);
            }
            case FROM_INSTANCEOF: {
                return tokenFactory.createToken(OQLTokenId.KEYWORD, input.readLength(), PartType.START);
            }
            case IN_CLASSNAME: {
                String lastToken = input.readText().toString().trim().toUpperCase();
                Matcher matcher = classPattern.matcher(lastToken);
                if (matcher.matches()) {
                    if ((isEmpty(matcher.group(1)) ? 0 : 1) + (isEmpty(matcher.group(2)) ? 0 : 1) > 1) {
                        return tokenFactory.createToken(OQLTokenId.ERROR);
                    }
                    state = State.CLASS_ALIAS;
                    input.backup(1);
                    return tokenFactory.createToken(OQLTokenId.CLAZZ);
                } else {
                    return tokenFactory.createToken(OQLTokenId.ERROR);
                }
            }
            case CLASS_ALIAS: {
                String lastToken = input.readText().toString().toUpperCase();

                if (TOKEN_SELECT.equals(lastToken) ||
                    TOKEN_FROM.equals(lastToken) ||
                    TOKEN_INSTANCEOF.equals(lastToken) ||
                    TOKEN_WHERE.equals(lastToken)) {
                    state = State.ERROR;
                    input.backup(lastToken.length());
                } else {
                    return tokenFactory.createToken(OQLTokenId.IDENTIFIER);
                }
            }
            case IN_WHERE: {
                return tokenFactory.createToken(OQLTokenId.KEYWORD, input.readLength(), PartType.START);
            }
            case ERROR: {
                return tokenFactory.createToken(OQLTokenId.ERROR);
            }
            default: {
                return tokenFactory.createToken(OQLTokenId.UNKNOWN);
            }
        }
    }

    final private static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public Object state () {
        return state;
    }

    public void release () {
    }


}


