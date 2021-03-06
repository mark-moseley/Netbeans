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

package org.netbeans.api.lexer;

/**
 * Token describes a lexical element of input text.
 * <br/>
 * It mainly provides an identification by {@link #id()}
 * and a textual body (aka token's image) by {@link #text()}.
 * <br/>
 * Only lexers should produce token instances and they should do it
 * solely by using methods of {@link org.netbeans.spi.lexer.TokenFactory}.
 *
 * <p>
 * <b>Note:</b><font color="red">
 * Do not create custom extensions of this class - lexers may only return
 * implementations produced by <code>TokenFactory</code>.
 * Creation of any other token implementations will be refused.
 * </font>
 * </p>
 *
 * <p>
 * Token guarantees stability of the {@link #id()} and {@link #length()} methods.
 * The {@link #hashCode()} and {@link #equals(Object)} methods
 * use the default implementations from <code>java.lang.Object</code>.
 * <br/>
 * The two tokens are only equal if they are the same object.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class Token<T extends TokenId> {
    
    /**
     * Create token instance.
     * @throws IllegalStateException if a non-lexer-module-implementation token
     *  is attempted to be created.
     */
    protected Token() {
        if (!(this instanceof org.netbeans.lib.lexer.token.AbstractToken)) {
            throw new IllegalStateException("Custom token implementations prohibited."); // NOI18N
        }
    }

    /**
     * Get identification of this token.
     *
     * @return non-null identification of this token.
     */
    public abstract T id();
    
    /**
     * Get text of this token (aka token's image) as a character sequence.
     * <br/>
     * This text usually corresponds to the characters present in the lexed text input
     * unless {@link #isCustomText()} returns true.
     *
     * <p>
     * <b>Note for mutable input sources:</b>
     * <br/>
     * This method should only be called
     * within a readonly (or read-write) transaction
     * over the underlying input source
     * (such as <code>javax.swing.text.Document.render()</code>
     * for Swing documents).
     * <br/>
     * The result returned by this method
     * is only valid within a readonly (or read-write) transaction
     * over the input source (method must be re-called
     * during the next readonly transaction).
     * </p>
     *
     * @return non-null, non-empty text of this token.
     *  It may be <code>null</code> in case the token was used
     *  for a mutable input and it was removed
     *  from the token list for the given input (but even in such case
     *  the text can be retained in certain cases).
     *
     *  <p>
     *  The behavior of <code>equals()</code> and <code>hashCode()</code>
     *  of the returned character sequence is generally undefined.
     *  <br/>
     *  The returned character sequence can NOT be compared to another
     *  character sequence by using its <code>equals()</code> method.
     *  <br/>
     *  {@link org.netbeans.api.lexer.TokenUtilities} contains
     *  utility methods related to token text comparing.
     *  </p>
     *
     *  <p>
     *  The returned text is just a pointer to the primary source of the data
     *  e.g. a swing document. The character data are not duplicated in the tokens.
     *  </p>
     */
    public abstract CharSequence text();
    
    /**
     * Check whether {@link #text()} returns a custom value that may differ
     * from the original content of the text input.
     * <br/>
     * Using custom text may be useful in case when only certain part of the token
     * is useful for the particular use and the token's text can be shrinked
     * and possibly a flyweight text can be used.
     * <br/>
     * Also this is useful when using lexers generated by various lexer generators
     * that generally allow to use a custom text in the produced tokens.
     *
     * @return true if the text of the token does not correspond
     *  to the original characters present in the text input being lexed.
     */
    public abstract boolean isCustomText();
    
    /**
     * Get number of characters in the original text input
     * that the token spans.
     * <br/>
     * Usually this is the same value like {@link #text()}</code>.length()</code>
     * unless {@link #isCustomText()} returns true.
     * <br/>
     * Also this method will return valid length in all cases even
     * when the text of the token could become <code>null</code>.
     *
     * @return >=0 length of the token.
     */
    public abstract int length();
    
    /**
     * Get the offset at which this token is present in the input
     * or <code>-1</code> if this token is flyweight (and therefore does not store offset).
     * <br/>
     * <b>Note:</b> Use of {@link TokenSequence#offset()} is usually preferred over
     * this method because it returns actual offset even for the flyweight tokens.
     * <br/>
     * If necessary the flyweight token may be replaced by regular token
     * by using {@link TokenSequence#offsetToken()}.
     *
     * <p>
     * The complexity of the method should generally be constant
     * regardless of the level of the language embedding.
     * </p>
     *
     * @param tokenHierarchy <code>null</code> should be passed
     *  (the parameter is reserved for future use when token hierarchy snapshots will be implemented).
     *
     * @return >=0 offset of the token in the input or <code>-1</code>
     *  if this token is flyweight.
     */
    public abstract int offset(TokenHierarchy<?> tokenHierarchy);
    
    /**
     * Checks whether this token instance is used for multiple occurrences
     * of this token in this or other inputs.
     * <br/>
     * For example keywords or operators are typically flyweight tokens
     * while e.g. identifiers are not flyweight as their text generally varies.
     * <br/>
     * Flyweight tokens may decrease the memory consumption for the tokens
     * considerably for frequently used tokens. For example a single space ' '
     * may be a useful flyweight token as it's used very often throughout a source.
     * The decision of what tokens are made flyweight is upon the implementor
     * of the particular language.
     *
     * <p>
     * If the token is flyweight its {@link #offset(TokenHierarchy)} returns -1.
     *
     * @return true if the token is flyweight or false otherwise.
     */
    public abstract boolean isFlyweight();
    
    /**
     * Check whether this token represents a complete token
     * or whether it's a part of a complete token.
     */
    public abstract PartType partType();

    /**
     * Quickly determine whether this token has any extra properties.
     */
    public abstract boolean hasProperties();
    
    /**
     * Get extra property of this token.
     * <br/>
     * The token properties are defined by the lexer upon token creation.
     * The clients of the API cannot set any property of the token.
     *
     * @param key non-null key of the property to get.
     * @return non-null value of the property or null if the property does not
     *  have any value.
     *
     * @see #hasProperties()
     */
    public abstract Object getProperty(Object key);

    /**
     * Make sure the default implementation of <code>hashCode()</code> is used
     * and the token can safely be used in maps.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }
    
    /**
     * Make sure the default implementation of <code>equals()</code> is used
     * and the token can safely be used in maps.
     */
    @Override
    public final boolean equals(Object o) {
        return super.equals(o);
    }

}
