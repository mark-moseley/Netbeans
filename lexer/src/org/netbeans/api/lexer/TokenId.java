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

package org.netbeans.api.lexer;

/**
 * Identifier of a token (could also be called a token-type).
 * <br>
 * It is not a token, because it does not contain
 * the text (also called image) of the token.
 *
 * <p>
 * Token ids are typically defined as enums by the following pattern:
 * <pre>
 * public enum JavaTokenId implements TokenId {
 *
 *     ERROR(null, "error"),
 *     IDENTIFIER(null, "identifier"),
 *     ABSTRACT("abstract", "keyword"),
 *     ...
 *     SEMICOLON(";", "separator"),
 *     ...
 *
 *
 *     private final String fixedText; // Used by lexer for production of flyweight tokens
 *
 *     private final String primaryCategory;
 *
 *     JavaTokenId(String fixedText, String primaryCategory) {
 *         this.fixedText = fixedText;
 *         this.primaryCategory = primaryCategory;
 *     }
 *
 *     public String fixedText() {
 *         return fixedText;
 *     }
 *
 *     public String primaryCategory() {
 *         return primaryCategory;
 *     }
 *
 * }
 * </pre>
 *
 * <p>
 * Token ids can also be generated (e.g. by lexer generation tools)
 * by using {@link org.netbeans.spi.lexer.LanguageHierarchy#newId(String,int,String)} method.
 * <br>
 * All token ids of a language must have both
 * unique ordinal and name.
 * <br/>
 * Token name should be all uppercase while token categories should be named
 * in lowercase.
 *
 * <p>
 * Detailed information and rules for naming can be found
 * in <A href="http://lexer.netbeans.org/doc/token-id-naming.html">TokenId Naming</A>.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenId {
    
    /**
     * Get name of this tokenId.
     * <p>
     * It can serve for several purposes such as finding
     * a possible style information for the given token.
     * </p>
     *
     * @return non-null unique name of the TokenId. The name must be unique
     *  among other TokenId instances of the particular language description
     *  where it is defined. The name should consist of
     *  uppercase alphanumeric letters and underscores only.
     */
    String name();

    /**
     * Get integer identification of this tokenId.
     *
     * @return numeric identification of this TokenId.
     *  <BR>
     *  Ordinal must be a non-negative
     *  integer unique among all the tokenIDs inside the language
     *  where it is declared.
     *  <BR>
     *  The ordinals are usually defined and adopted from lexer
     *  generator tool that generates the lexer for the given language.
     *  <BR>
     *  They do not have to be consecutive.
     *  <br>
     *  On they other hand there should
     *  not be big gaps (e.g. 100 or more) because
     *  indexing arrays are constructed based on the ordinal values
     *  so the length of the indexing array corresponds
     *  to the highest ordinal of all the token ids declared
     *  for the particular language.
     *  <BR>
     *  The intIds allow more efficient use
     *  of the tokenIds in switch-case statements.
     */
    int ordinal();

    /**
     * Get name of primary token category into which this token belongs.
     * <br/>
     * Other token categories for this id can be defined in the language hierarchy.
     *
     * @return name of the primary token category into which this token belongs
     *  or null if there is no primary category for this token.
     */
    String primaryCategory();
    
}
