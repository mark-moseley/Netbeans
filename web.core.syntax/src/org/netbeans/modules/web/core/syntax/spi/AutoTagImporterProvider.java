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

package org.netbeans.modules.web.core.syntax.spi;

import javax.swing.text.Document;

/**
 *
 * @author Petr Pisl
 */
/** Until NetBeans 5.5 the code completion usually offered
 * only tags of libraries, which are already imported in
 * the page. More user-friendly is when all possible tags
 * from libraries, which are on the classpath, are offered
 * with the  code completion. Similar to the java code
 * completion. Implementation of this class provides
 * the functionality that makes auto tag library definition
 * in the document. The way, how the tag library definition
 * is done, depends on the type of document.
 *
 * The implementation has to be registered in the default
 * filesystem (in layer file) in the folder
 * Editors/${mime-types}/AutoTagImportProviders
 */
public interface AutoTagImporterProvider {

    /** The method is called, when user select a tag in
     * the code completion window and the tag is inserted into
     * the document. The implementation has to decide,
     * whether  the tag library is already defined, whether
     * the tag library has to be imported and if necessary
     * write the tag library declaration into the document.
     *
     * @param doc document on which the declaration should be written.
     * @param prefix prefix of the library
     * @param uri uri of the library
     */
    public void importLibrary(Document doc, String prefix, String uri);

    /** The implementation has to returns a prefix for an library defined
     * in a document. It returns null if the prefix is not defined in the
     * document.
     * <p>This is useful for example for palette items. When user drops some
     * user tag from palette, then the client code should ask for the prefix, which
     * is defined for the document. If there is the library definition
     * in the document then this method returns the defined prefix and the user
     * tag will be completed with the prefix. If this method returns null
     * (there is not the library definition in the document), then the client code
     * should cold importLibrary method to  add the library definition
     * to the document. </p>
     *
     * @param doc document where the declaration should be found.
     * @param uri uri of the library
     * @return the prefix defined for the library from library definition in the document\
     * or null if there is not the library definition in the document.
     */
    public String getDefinedPrefix(Document doc, String uri);
}