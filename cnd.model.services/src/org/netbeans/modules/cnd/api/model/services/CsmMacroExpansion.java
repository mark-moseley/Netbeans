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
package org.netbeans.modules.cnd.api.model.services;

import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionViewProvider;
import org.openide.util.Lookup;

/**
 * Service that provides macro expansions.
 *
 * @author Nick Krasilnikov
 */
public final class CsmMacroExpansion {

    // Flags for document of macro expansion view panel
    /** Flag of macro expansion view document. */
    public static final String MACRO_EXPANSION_VIEW_DOCUMENT = "macro-expansion-view-document"; // NOI18N
    /** Flag of synchronized caret. */
    public static final String MACRO_EXPANSION_SYNC_CARET = "macro-expansion-sync-caret"; // NOI18N
    /** Flag of synchronized context. */
    public static final String MACRO_EXPANSION_SYNC_CONTEXT = "macro-expansion-sync-context"; // NOI18N

    /** A dummy providers that never returns any results.*/
    private static final CsmMacroExpansionDocProvider EMPTY_MACRO_EXPANSION_DOC_PROVIDER = new EmptyMacroExpansionDoc();
    /** A dummy providers that never returns any results.*/
    private static final CsmMacroExpansionViewProvider EMPTY_MACRO_EXPANSION_VIEW_PROVIDER = new EmptyMacroExpansionView();
    /** Default macro expansion provider. */
    private static CsmMacroExpansionDocProvider defaultMacroExpansionDocProvider;
    /** Default macro expansion view provider. */
    private static CsmMacroExpansionViewProvider defaultMacroExpansionViewProvider;

    /**
     * Constructor.
     */
    private CsmMacroExpansion() {
    }
    
    /** Static method to obtain the provider.
     * @return the provider
     */
    private static synchronized CsmMacroExpansionDocProvider getMacroExpansionDocProvider() {
        if (defaultMacroExpansionDocProvider != null) {
            return defaultMacroExpansionDocProvider;
        }
        defaultMacroExpansionDocProvider = Lookup.getDefault().lookup(CsmMacroExpansionDocProvider.class);
        return defaultMacroExpansionDocProvider == null ? EMPTY_MACRO_EXPANSION_DOC_PROVIDER : defaultMacroExpansionDocProvider;
    }

    /** Static method to obtain the provider.
     * @return the provider
     */
    private static synchronized CsmMacroExpansionViewProvider getMacroExpansionViewProvider() {
        if (defaultMacroExpansionViewProvider != null) {
            return defaultMacroExpansionViewProvider;
        }
        defaultMacroExpansionViewProvider = Lookup.getDefault().lookup(CsmMacroExpansionViewProvider.class);
        return defaultMacroExpansionViewProvider == null ? EMPTY_MACRO_EXPANSION_VIEW_PROVIDER : defaultMacroExpansionViewProvider;
    }

//    /**
//     * Returns instantiation of template.
//     *
//     * @param template - template for instantiation
//     * @param params - template paramrters
//     * @return - instantiation
//     */
//    public static String getExpandedText(CsmFile file, int startOffset, int endOffset) {
//        return getMacroExpansionProvider().getExpandedText(file, startOffset, endOffset);
//    }

    /**
     * Macro expands content of one document to another.
     *
     * @param inDoc - document for macro expansion
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @param outDoc - result
     * @return - number of expansions
     */
    public static int expand(Document inDoc, int startOffset, int endOffset, Document outDoc) {
        return getMacroExpansionDocProvider().expand(inDoc, startOffset, endOffset, outDoc);
    }

    /**
     * Macro expands content of the document.
     *
     * @param doc - document for macro expansion
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @return - expansion
     */
    public static String expand(Document doc, int startOffset, int endOffset) {
        return getMacroExpansionDocProvider().expand(doc, startOffset, endOffset);
    }

    /**
     * Macro expands content of the document.
     * If we already knew file for document it's better to use this function, because it's faster.
     *
     * @param doc - document for macro expansion
     * @param doc - file of the document
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @return - expansion
     */
    public static String expand(Document doc, CsmFile file, int startOffset, int endOffset) {
        return getMacroExpansionDocProvider().expand(doc, file, startOffset, endOffset);
    }

    /**
     * returns original text and expanded text for document on specified offset
     * @param doc document
     * @param offset offset in document
     * @return array of two elements [text in document, expanded text]
     */
    public static String[] getMacroExpansion(Document doc, int offset) {
        return getMacroExpansionDocProvider().getMacroExpansion(doc, offset);
    }
    /**
     * Transforms original offset to offset in expanded text.
     *
     * @param expandedDoc - document
     * @param originalOffset - original offset
     * @return offset in expanded text
     */
    public static int getOffsetInExpandedText(Document expandedDoc, int originalOffset) {
        return getMacroExpansionDocProvider().getOffsetInExpandedText(expandedDoc, originalOffset);
    }

    /**
     * Transforms offset in expanded text to original offset.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return original offset
     */
    public static int getOffsetInOriginalText(Document expandedDoc, int expandedOffset) {
        return getMacroExpansionDocProvider().getOffsetInOriginalText(expandedDoc, expandedOffset);
    }

    /**
     * Returns offset of the next macro expansion.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return offset of the next macro expansion
     */
    public static int getNextMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        return getMacroExpansionDocProvider().getNextMacroExpansionStartOffset(expandedDoc, expandedOffset);
    }

    /**
     * Returns offset of the previous macro expansion.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return offset of the next macro expansion
     */
    public static int getPrevMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        return getMacroExpansionDocProvider().getPrevMacroExpansionStartOffset(expandedDoc, expandedOffset);
    }

    /**
     * Expands document on specified position and shows Macro Expansion View panel.
     *
     * @param doc - document
     * @param offset - offset in document
     */
    public static void showMacroExpansionView(Document doc, int offset) {
        getMacroExpansionViewProvider().showMacroExpansionView(doc, offset);
    }

    //
    // Implementation of the default provider
    //
    private static final class EmptyMacroExpansionDoc implements CsmMacroExpansionDocProvider {

        EmptyMacroExpansionDoc() {
        }

        public int expand(Document inDoc, int startOffset, int endOffset, Document outDoc) {
            return 0;
        }

        public String expand(Document doc, int startOffset, int endOffset) {
            return null;
        }

        public String expand(Document doc, CsmFile file, int startOffset, int endOffset) {
            return null;
        }
        
        public String[] getMacroExpansion(Document doc, int offset) {
            // returns empty expansion
            return new String[] {"", ""}; // NOI18N
        }
        
        public int getOffsetInExpandedText(Document expandedDoc, int originalOffset) {
            return originalOffset;
        }

        public int getOffsetInOriginalText(Document expandedDoc, int expandedOffset) {
            return expandedOffset;
        }

        public int getNextMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
            return expandedOffset;
        }

        public int getPrevMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
            return expandedOffset;
        }
    }

    //
    // Implementation of the default provider
    //
    private static final class EmptyMacroExpansionView implements CsmMacroExpansionViewProvider {

        EmptyMacroExpansionView() {
        }

        public void showMacroExpansionView(Document doc, int offset) {
        }

    }
}
