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

import org.netbeans.lib.lexer.TokenOrEmbedding;
import java.util.List;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.TokenList;

/**
 * Abstract token is base class of all token implementations used in the lexer module.
 * <br/>
 * Two descendants of AbstractToken:
 * <ul>
 *   <li>{@link DefaultToken} - by default does not contain a text but points
 *       into a text storage of its token list instead. It may however cache
 *       its text as string in itself.
 *       <ul>
 *           <li></li>
 *       </ul>
 *   </li>
 *   <li>{@link TextToken} - contains text that it represents; may act as flyweight token.
 *       {@link CustomTextToken} allows a token to have a custom text independent
 *       of text of an actual storage.
 *   </li>
 * 
 * 
 *
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class AbstractToken<T extends TokenId> extends Token<T>
implements TokenOrEmbedding<T> {
    
    private final T id; // 12 bytes (8-super + 4)

    protected TokenList<T> tokenList; // 16 bytes
    
    protected int rawOffset; // 20 bytes

    /**
     * @id non-null token id.
     */
    public AbstractToken(T id) {
        assert (id != null);
        this.id = id;
    }
    
    AbstractToken(T id, TokenList<T> tokenList, int rawOffset) {
        this.id = id;
        this.tokenList = tokenList;
        this.rawOffset = rawOffset;
    }
    
    /**
     * Get identification of this token.
     *
     * @return non-null identification of this token.
     */
    @Override
    public final T id() {
        return id;
    }

    /**
     * Get token list to which this token delegates its operation.
     */
    public final TokenList<T> tokenList() {
        return tokenList;
    }

    /**
     * Release this token from being attached to its parent token list.
     */
    public final void setTokenList(TokenList<T> tokenList) {
        this.tokenList = tokenList;
    }

    /**
     * Get raw offset of this token.
     * <br/>
     * Raw offset must be preprocessed before obtaining the real offset.
     */
    public final int rawOffset() {
        return rawOffset;
    }

    /**
     * Set raw offset of this token.
     *
     * @param rawOffset new raw offset.
     */
    public final void setRawOffset(int rawOffset) {
        this.rawOffset = rawOffset;
    }

    @Override
    public final boolean isFlyweight() {
        return (rawOffset == -1);
    }

    public final void makeFlyweight() {
        setRawOffset(-1);
    }
    
    @Override
    public PartType partType() {
        return PartType.COMPLETE;
    }

    @Override
    public boolean isCustomText() {
        return false;
    }

    @Override
    public int offset(TokenHierarchy<?> tokenHierarchy) {
        if (tokenList != null) {
            if (tokenList.getClass() == EmbeddedTokenList.class) // Sync status first
                ((EmbeddedTokenList)tokenList).embeddingContainer().updateStatus();
            return tokenList.tokenOffset(this);
        }
        return rawOffset; // Covers the case of flyweight token that will return -1
//        if (tokenHierarchy != null) {
//            return LexerApiPackageAccessor.get().tokenHierarchyOperation(
//                    tokenHierarchy).tokenOffset(this, tokenList, rawOffset);
//        } else {
//            return (tokenList != null)
//                ? tokenList.childTokenOffset(rawOffset)
//                : rawOffset;
//        }
    }

    @Override
    public boolean hasProperties() {
        return false;
    }
    
    @Override
    public Object getProperty(Object key) {
        return null;
    }

    @Override
    public Token<T> joinToken() {
        return null;
    }

    @Override
    public List<? extends Token<T>> joinedParts() {
        return null;
    }

    // Implements TokenOrEmbedding
    public final AbstractToken<T> token() {
        return this;
    }
    
    // Implements TokenOrEmbedding
    public final EmbeddingContainer<T> embedding() {
        return null;
    }

    @Override
    public boolean isRemoved() {
        if (tokenList != null) {
            if (tokenList.getClass() == EmbeddedTokenList.class)
                ((EmbeddedTokenList)tokenList).embeddingContainer().updateStatus();
            return tokenList.isRemoved();
        }
        return !isFlyweight();
    }

    public String dumpInfo() {
        return dumpInfo(null, null, true, true, 0).toString();
    }

    /**
     * Dump various information about this token
     * into a string for debugging purporses.
     * <br>
     * A regular <code>toString()</code> usually just returns
     * a text of the token to satisfy acting of the token instance
     * as <code>CharSequence</code>.
     *
     * @param tokenHierarchy <code>null</code> should be passed
     *  (the parameter is reserved for future use when token hierarchy snapshots will be implemented).
     * @param dumpText whether text should be dumped (not for TokenListUpdater
     *  when text is already shifted).
     * @param dumpRealOffset whether real offset should be dumped or whether raw offset should be used.
     * @return dump of the thorough token information. If token's text is longer
     *  than 400 characters it will be shortened.
     */
    public StringBuilder dumpInfo(StringBuilder sb, TokenHierarchy<?> tokenHierarchy,
            boolean dumpText, boolean dumpRealOffset, int indent
    ) {
        if (sb == null) {
            sb = new StringBuilder(50);
        }
        if (dumpText) {
            try {
                CharSequence text = text();
                if (text != null) {
                    sb.append('"');
                    int textLength = text.length();
                    for (int i = 0; i < textLength; i++) {
                        if (textLength > 400 && i >= 200 && i < textLength - 200) {
                            i = textLength - 200;
                            sb.append(" ...<TEXT-SHORTENED>... "); // NOI18N
                            continue;
                        }
                        try {
                            CharSequenceUtilities.debugChar(sb, text.charAt(i));
                        } catch (IndexOutOfBoundsException e) {
                            // For debugging purposes it's better than to completely fail
                            sb.append("IOOBE at index=").append(i).append("!!!"); // NOI18N
                            break;
                        }
                    }
                    sb.append('"');
                } else {
                    sb.append("<null-text>"); // NOI18N
                }
            } catch (NullPointerException e) {
                sb.append("NPE in Token.text()!!!"); // NOI18N
            }
            sb.append(' ');
        }
        if (isFlyweight()) {
            sb.append("F(").append(length()).append(')');
        } else {
            int offset = dumpRealOffset ? offset(tokenHierarchy) : rawOffset();
            sb.append('<').append(offset); // NOI18N
            sb.append(",").append(offset + length()).append('>'); // NOI18N
        }
        sb.append(' ').append(id != null ? id.name() + '[' + id.ordinal() + ']' : "<null-id>"); // NOI18N
        sb.append(" ").append(dumpInfoTokenType());
        return sb;
    }
    
    protected String dumpInfoTokenType() {
        return "AbsT"; // NOI18N "AbstractToken"
    }
    
}
