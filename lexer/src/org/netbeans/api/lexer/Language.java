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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.LexerSpiPackageAccessor;
import org.netbeans.lib.lexer.TokenIdSet;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.spi.lexer.LanguageHierarchy;

/**
 * Language describes a set of token ids
 * that comprise the given language.
 * <br/>
 * Each language corresponds to a certain mime-type.
 * <br/>
 * An input source may be lexed by using an existing language
 * - see {@link TokenHierarchy} which is an entry point into the Lexer API.
 * <br>
 * Language hierarchy is represented by an unmodifiable set of {@link TokenId}s
 * that can be retrieved by {@link #tokenIds()} and token categories
 * {@link #tokenCategories()}.
 *
 * <p>
 * The language cannot be instantiated directly.
 * <br/>
 * Instead it should be obtained from {@link LanguageHierarchy#language()}
 * on an existing language hierarchy.
 *
 * @see LanguageHierarchy
 * @see TokenId
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class Language<T extends TokenId> {
    
    static {
        LexerApiPackageAccessor.register(new Accessor());
    }
    
    private final LanguageHierarchy<T> languageHierarchy;
    
    private String mimeType;
    
    private final int maxOrdinal;

    private final Set<T> ids;

    /** Lazily inited indexed ids for quick translation of ordinal to token id. */
    private TokenIdSet<T> indexedIds;

    private final Map<String,T> idName2id;
    
    /**
     * Map of category to ids that it contains.
     */
    private final Map<String,Set<T>> cat2ids;
    
    /**
     * Lists of token categories for particular id.
     * <br/>
     * It's a list because it is ordered (primary category is first).
     */
    private List<String>[] id2cats;
    
    /**
     * Lists of non-primary token categories for particular id.
     * <br/>
     * It's a list because the order might be important
     * (e.g. for syntax coloring information resolving) although
     * the present SPI does not utilize that.
     */
    private List<String>[] id2nonPrimaryCats;
    
    /**
     * Finds a language by its mime type.
     * 
     * <p>This method uses information from <code>LanguageProvider</code>s registered
     * in the default lookup to find <code>Language</code> for a given
     * mime path.
     * 
     * <div class="nonnormative">
     * <p>Netbeans provide an implementation of <code>LanguageProvider</code>
     * that reads data from the <code>Editors</code> folder on the system filesystem.
     * Therefore Netbeans modules can register their <code>Language</code>s
     * in MimeLookup as any other mime-type related service.
     * 
     * <p>Since this method takes <code>mimePath</code> as a parameter it is
     * possible to look up <code>Language</code> registered for
     * a mime type that is embedded in some other mime type (i.e. the nesting
     * can go any number of levels deep). In reality, however, there is usually
     * no difference between a language, which uses its own file and the same
     * language embedded in some other language's file (e.g. there is no difference
     * between java language in <code>hello.java</code> and a java scriplet in
     * <code>goodbye.jsp</code>). Therefore <code>Language</code>s are
     * usually registered only for top level mime types and if a language
     * can be embedded in another language the embedded language is referenced
     * by its top level mime type.
     * </div>
     * 
     * @param mimePath The mime path of the language you want to find.
     * @return The <code>Language</code> of the language registered
     *         for the given <code>mimePath</code>.
     */
    public static Language<? extends TokenId> find(String mimePath) {
        return LanguageManager.getInstance().findLanguage(mimePath);
    }
    
    /**
     * Construct language by providing a collection of token ids
     * that comprise the language and extra categories into which the token ids belong.
     *
     * @param languageHierarchy non-null language hierarchy is in one-to-one relationship
     *  with the language and represents it on SPI side.
     * @throws IndexOutOfBoundsException if any token id's ordinal is &lt; 0.
     */
    Language(LanguageHierarchy<T> languageHierarchy) {
        this.languageHierarchy = languageHierarchy;
        mimeType = LexerSpiPackageAccessor.get().mimeType(languageHierarchy);
        checkMimeTypeValid(mimeType);
        // Create ids and find max ordinal
        Collection<T> createdIds = LexerSpiPackageAccessor.get().createTokenIds(languageHierarchy);
        if (createdIds == null)
            throw new IllegalArgumentException("Ids cannot be null"); // NOI18N
        maxOrdinal = TokenIdSet.findMaxOrdinal(createdIds);

        // Convert collection of ids to efficient indexed Set<T>
        if (createdIds instanceof EnumSet) {
            ids = (Set<T>)createdIds;
        } else { // not EnumSet
            ids = new TokenIdSet<T>(createdIds, maxOrdinal, true);
        }
        
        // Create TokenIdSet instances for token categories
        Map<String,Collection<T>> createdCat2ids
                = LexerSpiPackageAccessor.get().createTokenCategories(languageHierarchy);
        if (createdCat2ids == null) {
            createdCat2ids = Collections.emptyMap();
        }
        cat2ids = new HashMap<String,Set<T>>((int)(createdCat2ids.size() / 0.73f));
        for (Map.Entry<String,Collection<T>> entry : createdCat2ids.entrySet()) {
            Collection<T> createdCatIds = entry.getValue();
            TokenIdSet.checkIdsFromLanguage(createdCatIds, ids);
            // Do not use the original createdCatIds set because of the following:
            // 1) Two token categories will have the same sets of contained ids
            //   in the createdCatIds map (the same physical Set instance).
            // 2) At least one token id will have one of the two categories
            //   as its primary category.
            // 3) If the original Set instance from the createdCatIds would be used
            //   then both categories would incorrectly contain the extra id(s).
            Set<T> catIds = new TokenIdSet<T>(createdCatIds, maxOrdinal, false);
            cat2ids.put(entry.getKey(), catIds);
        }

        // Walk through all ids and check duplicate names and primary categories
        idName2id = new HashMap<String,T>((int)(ids.size() / 0.73f));
        for (T id : ids) {
            T sameNameId = idName2id.put(id.name(), id);
            if (sameNameId != null && sameNameId != id) { // token ids with same name
                throw new IllegalArgumentException(id +
                        " has duplicate name with " + sameNameId);
            }

            String cat = id.primaryCategory();
            if (cat != null) {
                Set<T> catIds = cat2ids.get(cat);
                if (catIds == null) {
                    catIds = new TokenIdSet<T>(null, maxOrdinal, false);
                    cat2ids.put(cat, catIds);
                }
                catIds.add(id);
            }
        }
    }
    
    /**
     * Get unmodifiable set of ids contained in this language.
     * <br/>
     * An iterator over the set returns the ids sorted by their ordinals.
     *
     * @return unmodifiable set of ids contained in this language.
     */
    public Set<T> tokenIds() {
        return ids;
    }
    
    /**
     * Get tokenId for the given ordinal. This method
     * can be used by lexers to quickly translate ordinal
     * to tokenId.
     * @param ordinal ordinal to be translated to corresponding tokenId.
     * @return valid tokenId or null if there's no corresponding
     *  tokenId for the given int-id. It's possible because intIds
     *  of the language's token ids do not need to be continuous.
     *  If the ordinal is &lt;0 or higher than the highest
     *  ordinal of all the token ids of this language the method
     *  throws {@link IndexOutOfBoundsException}.
     * @throws IndexOutOfBoundsException if the ordinal is
     *  &lt;0 or higher than {@link #maxOrdinal()}.
     */
    public T tokenId(int ordinal) {
        synchronized (idName2id) {
            if (indexedIds == null) {
                if (ids instanceof EnumSet) {
                    indexedIds = new TokenIdSet<T>(ids, maxOrdinal, false);
                } else { // not EnumSet
                    indexedIds = (TokenIdSet<T>)ids;
                }
            }
            return indexedIds.indexedIds()[ordinal];
        }
    }
    
    /**
     * Similar to {@link #tokenId(int)} however it guarantees
     * that it will always return non-null tokenId. Typically for a lexer 
     * just being developed it's possible that there are some integer
     * token ids defined in the generated lexer for which there is
     * no correspondence in the language. The lexer wrapper should
     * always call this method if it expects to find a valid
     * counterpart for given integer id.
     * @param ordinal ordinal to translate to token id.
     * @return always non-null tokenId that corresponds to the given integer id.
     * @throws IndexOutOfBoundsException if the ordinal is
     *  &lt;0 or higher than {@link #maxOrdinal()} or when there is no corresponding
     *  token id for it.
     */
    public T validTokenId(int ordinal) {
        T id = tokenId(ordinal);
        if (id == null) {
            throw new IndexOutOfBoundsException("No tokenId for ordinal=" + ordinal
                + " in language " + this);
        }
        return id;
    }
    
    /**
     * Find the tokenId from its name.
     * @param name name of the tokenId to find.
     * @return tokenId with the requested name or null if it does not exist.
     */
    public T tokenId(String name) {
        return idName2id.get(name);
    }
    
    /**
     * Similar to {@link #tokenId(String)} but guarantees a valid tokenId to be returned.
     * @throws IllegalArgumentException if no token in this language has the given name.
     */
    public T validTokenId(String name) {
        T id = tokenId(name);
        if (id == null) {
            throw new IllegalArgumentException("No tokenId for name=\"" + name
                + "\" in language " + this);
        }
        return id;
    }
    
    /**
     * Get maximum ordinal of all the token ids that this language contains.
     * @return maximum integer ordinal of all the token ids that this language contains
     *  or <code>-1</code> if the language contains no token ids.
     */
    public int maxOrdinal() {
        return maxOrdinal;
    }

    /**
     * Get names of all token categories of this language.
     *
     * @return unmodifiable set containing names of all token categories
     *  contained in this language.
     */
    public Set<String> tokenCategories() {
        return Collections.unmodifiableSet(cat2ids.keySet());
    }

    /**
     * Get members of the category with given name.
     *
     * @param tokenCategory non-null name of the category.
     * @return set of token ids belonging to the given category.
     */
    public Set<T> tokenCategoryMembers(String tokenCategory) {
        return Collections.unmodifiableSet(cat2ids.get(tokenCategory));
    }
    
    /**
     * Get list of all token categories for the particular token id.
     *
     * @return non-null unmodifiable list of all token categories for the particular token id.
     *  <br>
     *  Primary token's category (if defined for the token id) will be contained
     *  as first one in the list.
     * @throws IllegalArgumentException if the given token id does not belong
     *  to this language.
     */
    public List<String> tokenCategories(T tokenId) {
        checkMemberId(tokenId);
        synchronized (idName2id) {
            if (id2cats == null) {
                buildTokenIdCategories();
            }
            return id2cats[tokenId.ordinal()];
        }
    }
    
    /**
     * Get list of non-primary token categories (not containing the primary category)
     * for the particular token id.
     * <br/>
     * If the token id has no primary category defined then the result
     * of this method is equal to {@link #tokenCategories(TokenId)}.
     *
     * @return non-null unmodifiable list of secondary token categories for the particular token id.
     *  Primary token's category (if defined for the token id) will not be contained
     *  in the list.
     * @throws IllegalArgumentException if the given token id does not belong
     *  to this language.
     */
    public List<String> nonPrimaryTokenCategories(T tokenId) {
        checkMemberId(tokenId);
        synchronized (idName2id) {
            if (id2nonPrimaryCats == null) {
                buildTokenIdCategories();
            }
            return id2nonPrimaryCats[tokenId.ordinal()];
        }
    }
    
    /**
     * Merge two collections of token ids from this language
     * into an efficient indexed set (the implementation similar
     * to {@link java.util.EnumSet}).
     *
     * @param tokenIds1 non-null collection of token ids to be contained in the returned set.
     * @param tokenIds2 collection of token ids to be contained in the returned set.
     * @return set of token ids indexed by their ordinal number.
     */
    public Set<T> merge(Collection<T> tokenIds1, Collection<T> tokenIds2) {
        TokenIdSet.checkIdsFromLanguage(tokenIds1, ids);
        // Cannot retain EnumSet as tokenIds will already be wrapped
        // by unmodifiableSet()
        Set<T> ret = new TokenIdSet<T>(tokenIds1, maxOrdinal, false);
        if (tokenIds2 != null) {
            TokenIdSet.checkIdsFromLanguage(tokenIds2, ids);
            ret.addAll(tokenIds2);
        }
        return ret;
    }

    /**
     * Gets the mime type of this language.
     *
     * @return non-null language's mime type.
     */
    public String mimeType() {
        return mimeType;
    }
    
    /** The languages are equal only if they are the same objects. */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    /** The hashCode of the language is the identity hashCode. */
    public int hashCode() {
        return super.hashCode();
    }

    private void buildTokenIdCategories() {
        assignCatArrays();
        // List for decreasing of the number of created maps
        // for tokenId2category mappings.
        // List.get(0) is a Map[category, list-of-[category]].
        // List.get(1) is a Map[category1, Map[category2, list-of-[category1;category2]]].
        // etc.
        List<Map<String,Object>> catMapsList = new ArrayList<Map<String,Object>>(4);
        // All categories for a single token id
        List<String> idCats = new ArrayList<String>(4);
        for (T id : ids) {
            // No extra sorting of the categories in which the particular id is contained
            //  - making explicit order of the categories could possibly be acomplished
            //    in the future if necessary by supporting some extra hints
            // Add all the categories for the particular id into idCats
            for (Map.Entry<String,Set<T>> e : cat2ids.entrySet()) {
                if (e.getValue().contains(id)) {
                    idCats.add(e.getKey()); // Add this category to id's categories
                }
            }
            // Assign both non-primary cats and all cats
            id2cats[id.ordinal()] = findCatList(catMapsList, idCats, 0);
            id2nonPrimaryCats[id.ordinal()] = findCatList(catMapsList, idCats, 1);

            idCats.clear(); // reuse the list (is cloned if added to catMapsList)
        }
    }

    /**
     * Find the cached list of categories from the catMapsList
     * for the particular temporarily collected list of categories.
     *
     * @param catMapsList non-null list of cached maps.
     *  <br/>
     *  List.get(0) is a Map[category, list-containing-[category]].
     *  <br/>
     *  List.get(1) is a Map[category1, Map[category2, list-containing-[category1;category2]]].
     *  <br/>
     *  etc.
     *
     * @param idCats non-null temporarily collected list of categories for the particular id.
     *  It may be modified after this method gets finished.
     * @param startIndex >=0 starting index in idCats - either 0 for returning
     * of all categories or 1 for returning non-primary categories.
     * @return non-null cached list of categories with contents equal to idCats.
     */
    private static List<String> findCatList(List<Map<String,Object>> catMapsList, List<String> idCats, int startIndex) {
        int size = idCats.size() - startIndex;
        if (size <= 0) {
            return Collections.emptyList();
        }
        while (catMapsList.size() < size) {
            catMapsList.add(new HashMap<String,Object>());
        }
        // Find the catList as the last item in the cascaded search through the maps
        Map<String,Object> m = catMapsList.get(--size);
        for (int i = startIndex; i < size; i++) {
            @SuppressWarnings("unchecked")
            Map<String,Object> catMap = (Map<String,Object>)m.get(idCats.get(i));
            if (catMap == null) {
                catMap = new HashMap<String,Object>();
//                Map<String,Map<String,Object>> 
                m.put(idCats.get(i), catMap);
            }
            m = catMap;
        }

        @SuppressWarnings("unchecked")
        List<String> catList = (List<String>)m.get(idCats.get(size));
        if (catList == null) {
            catList = new ArrayList<String>(idCats.size() - startIndex);
            catList.addAll((startIndex > 0)
                    ? idCats.subList(startIndex, idCats.size())
                    : idCats);
            m.put(idCats.get(size), catList);
        }
        return catList;
    }
        
    @SuppressWarnings("unchecked")
    private void assignCatArrays() {
        id2cats = (List<String>[])new List[maxOrdinal + 1];
        id2nonPrimaryCats = (List<String>[])new List[maxOrdinal + 1];
    }

    /**
     * Dump list of token ids for this language into string.
     *
     * @return dump of contents of this language.
     */
    public String dumpInfo() {
        StringBuilder sb = new StringBuilder();
        for (T id : ids) {
            sb.append(id);
            List<String> cats = tokenCategories(id);
            if (cats.size() > 0) {
                sb.append(": ");
                for (int i = 0; i < cats.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    String cat = (String)cats.get(i);
                    sb.append('"');
                    sb.append(cat);
                    sb.append('"');
                }
            }
        }
        return ids.toString();
    }
    
    public String toString() {
        return mimeType + ", LH: " + languageHierarchy;
    }
    
    private void checkMemberId(T id) {
        if (!ids.contains(id)) {
            throw new IllegalArgumentException(id + " does not belong to language " + this); // NOI18N
        }
    }
    
    private static void checkMimeTypeValid(String mimeType) {
        if (mimeType == null) {
            throw new IllegalStateException("mimeType cannot be null"); // NOI18N
        }
        int slashIndex = mimeType.indexOf('/');
        if (slashIndex == -1) { // no slash
            throw new IllegalStateException("mimeType=" + mimeType + " does not contain '/'"); // NOI18N
        }
        if (mimeType.indexOf('/', slashIndex + 1) != -1) {
            throw new IllegalStateException("mimeType=" + mimeType + " contains more than one '/'"); // NOI18N
        }
    }
    
    /**
     * Return language hierarchy associated with this language.
     * <br>
     * This method is for API package accessor only.
     */
    LanguageHierarchy<T> languageHierarchy() {
        return languageHierarchy;
    }
        
    /**
     * Accessor of package-private things in this package
     * that need to be used by the lexer implementation classes.
     */
    private static final class Accessor extends LexerApiPackageAccessor {

        public <T extends TokenId> Language<T> createLanguage(
        LanguageHierarchy<T> languageHierarchy) {
            return new Language<T>(languageHierarchy);
        }
        
        public <T extends TokenId> LanguageHierarchy<T> languageHierarchy(
        Language<T> language) {
            return language.languageHierarchy();
        }
        
        public <I> TokenHierarchy<I> createTokenHierarchy(
        TokenHierarchyOperation<I,?> tokenHierarchyOperation) {
            return new TokenHierarchy<I>(tokenHierarchyOperation);
        }
        
        public TokenHierarchyEvent createTokenChangeEvent(
        TokenHierarchyEventInfo info) {
            return new TokenHierarchyEvent(info);
        }

        public <T extends TokenId> TokenChange<T> createTokenChange(
        TokenChangeInfo<T> info) {
            return new TokenChange<T>(info);
        }
        
        public <T extends TokenId> TokenChangeInfo<T> tokenChangeInfo(
        TokenChange<T> tokenChange) {
            return tokenChange.info();
        }
        
        public <I> TokenHierarchyOperation<I,?> tokenHierarchyOperation(
        TokenHierarchy<I> tokenHierarchy) {
            return tokenHierarchy.operation();
        }

    }

}

