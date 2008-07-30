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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenList;

/**
 * Token with an explicit text - either serving a flyweight token
 * or a non-flyweight replacement for a flyweight token.
 * <br/>
 * The represented text should be the same like the original content
 * of the recognized text input portion.
 *
 * <p>
 * The text token can act as a flyweight token by calling
 * {@link AbstractToken.makeFlyweight()}. In such case a single token
 * instance is shared for all the occurrences of the token.
 * <br/>
 * The rawOffset is -1 and tokenList reference is null.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TextToken<T extends TokenId> extends AbstractToken<T> {
    
    private final CharSequence text; // 24 bytes (20-super + 4)

    /**
     * Create text token. The token's text
     * is expected to correspond to the recognized input portion
     * (i.e. the text is not custom).
     * <br/>
     * The token can be made flyweight by using <code>makeFlyweight()</code>.
     *
     * @param id non-null identification of the token.
     * @param text non-null text of the token.
     */
    public TextToken(T id, CharSequence text) {
        super(id);
        assert (text != null);
        this.text = text;
    }
    
    private TextToken(T id, int rawOffset, CharSequence text) {
        super(id, rawOffset);
        assert (text != null);
        this.text = text;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public final CharSequence text() {
        return text;
    }
    
    public final TextToken<T> createCopy(TokenList<T> tokenList, int rawOffset) {
        TextToken<T> token = new TextToken<T>(id(), rawOffset, text);
        token.setTokenList(tokenList);
        return token;
    }
    
    @Override
    protected String dumpInfoTokenType() {
        return isFlyweight() ? "FlyT" : "TexT"; // NOI18N "TextToken" or "FlyToken"
    }

    @Override
    public String toString() {
        return text.toString();
    }

}
