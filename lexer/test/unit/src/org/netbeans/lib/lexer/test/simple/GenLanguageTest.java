/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test.simple;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;


/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class GenLanguageTest extends TestCase {
    
    private static final int IDS_SIZE = 13;
    
    public GenLanguageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenIds() {
        // Check that token ids are all present and correctly ordered
        Language language  = GenLanguage.language();
        Set ids = language.tokenIds();
        assertTrue("Invalid ids.size() - expected " + IDS_SIZE, ids.size() == IDS_SIZE);
        
        TokenId[] idArray = {
            GenLanguage.IDENTIFIER_ID,
            GenLanguage.PLUS_ID,
            GenLanguage.MINUS_ID,
            GenLanguage.PLUS_MINUS_PLUS_ID,
            GenLanguage.SLASH_ID,
            GenLanguage.STAR_ID,
            GenLanguage.ML_COMMENT_ID,
            GenLanguage.WHITESPACE_ID,
            GenLanguage.SL_COMMENT_ID,
            GenLanguage.ERROR_ID,
            GenLanguage.PUBLIC_ID,
            GenLanguage.PRIVATE_ID,
            GenLanguage.STATIC_ID,
        };

        // Check operations with ids
        Collection testIds = Arrays.asList(idArray);
        LexerTestUtilities.assertCollectionsEqual("Ids do not match with test ones",
                ids, testIds);
        
        // Check that ids.iterator() is ordered by ordinal
        int ind = 0;
        for (Iterator it = ids.iterator(); it.hasNext();) {
            TokenId id = (TokenId) it.next();
            assertTrue("Token ids not sorted by ordinal at index=" + ind, id == idArray[ind]);
            ind++;
            assertSame(language.tokenId(id.name()), id);
            assertSame(language.tokenId(id.ordinal()), id);
            assertSame(language.validTokenId(id.name()), id);
            assertSame(language.validTokenId(id.ordinal()), id);
        }
        
        try {
            language.validTokenId("invalid-name");
            fail("Error: exception not thrown");
        } catch (IllegalArgumentException e) {
            // OK
        }
        
        try {
            language.validTokenId(-1);
            fail("Error: exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // OK
        }
        
        try {
            language.validTokenId(20);
            fail("Error: exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // OK
        }
        
        assertEquals(15, language.maxOrdinal());
        
        // Check token categories
        Set cats = language.tokenCategories();
        Collection testCats = Arrays.asList(new String[] { "operator", "test-category", "whitespace", "error", "comment", "keyword" });
        LexerTestUtilities.assertCollectionsEqual("Invalid token categories",
                cats, testCats);
        
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    GenLanguage.PLUS_ID,
                    GenLanguage.MINUS_ID,
                    GenLanguage.PLUS_MINUS_PLUS_ID,
                    GenLanguage.STAR_ID,
                    GenLanguage.SLASH_ID,
                }),
                language.tokenCategoryMembers("operator")
        );
        
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    GenLanguage.PLUS_ID,
                    GenLanguage.MINUS_ID,
                    GenLanguage.IDENTIFIER_ID,
                }),
                language.tokenCategoryMembers("test-category")
        );

        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    GenLanguage.WHITESPACE_ID,
                }),
                language.tokenCategoryMembers("whitespace")
        );

        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    GenLanguage.ERROR_ID,
                }),
                language.tokenCategoryMembers("error")
        );

        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    GenLanguage.ML_COMMENT_ID,
                    GenLanguage.SL_COMMENT_ID,
                }),
                language.tokenCategoryMembers("comment")
        );
                
        @SuppressWarnings("unchecked") List testIdCats
            = language.tokenCategories(GenLanguage.IDENTIFIER_ID);
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new String[] {
                    "test-category",
                }),
                testIdCats
        );

        @SuppressWarnings("unchecked") List testIdCats2
            = language.tokenCategories(GenLanguage.PLUS_ID);
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new String[] {
                    "test-category",
                    "operator",
                }),
                testIdCats2
        );


        // Check Language.merge()
        @SuppressWarnings("unchecked") Collection mergedIds
                = language.merge(
                    Arrays.asList(new TokenId[] { GenLanguage.IDENTIFIER_ID }),
                    language.merge(language.tokenCategoryMembers("comment"),
                        language.tokenCategoryMembers("error"))
                );
        LexerTestUtilities.assertCollectionsEqual("Invalid language.merge()",
                Arrays.asList(new TokenId[] {
                    GenLanguage.ML_COMMENT_ID,
                    GenLanguage.SL_COMMENT_ID,
                    GenLanguage.ERROR_ID,
                    GenLanguage.IDENTIFIER_ID,
                }),
                mergedIds
        );

    }

}
