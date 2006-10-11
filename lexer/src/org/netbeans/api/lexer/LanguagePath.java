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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Language path describes a complete embedding
 * of the language descriptions starting from the root (top-level) language
 * till the most embedded language.
 * <br/>
 * Language path consists of one root language description
 * and zero or more embedded descriptions.
 * <br/>
 * E.g. for javadoc embedded in java that is embedded in jsp
 * then the language path <code>lp</code> would return the following:<pre>
 *  lp.size() == 3
 *  lp.language(0) == JspLanguage.description()
 *  lp.language(1) == JavaLanguage.description()
 *  lp.language(2) == JavadocLanguage.description()
 * </pre>
 *
 * <p>
 * The two language paths for the same languages in the same order
 * represent a single object. Therefore language paths can be compared
 * by using == operator.
 * </p>
 *
 * <p>
 * <b>Lifetime:</b>
 * Once a particular language path is created
 * it is held by a soft reference from its "parent" language path.
 * </p>
 *
 * <p>
 * This class may safely be used by multiple threads.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.0
 */
public final class LanguagePath {
    
    /**
     * Empty language path for internal use and referencing the top-level language paths.
     */
    private static final LanguagePath EMPTY = new LanguagePath();

    /**
     * Get language path that contains a single language description.
     *
     * @param language non-null language.
     * @return non-null language path.
     */
    public static LanguagePath get(LanguageDescription<? extends TokenId> language) {
        return get(EMPTY, language);
    }
    
    /**
     * Get language path corresponding to the language embedded in the given context
     * language path.
     * <br/>
     * For example for java scriplet embedded in jsp the prefix would 
     * be a language-path for jsp language and language would be java language.
     * <br/>
     * By using this method language paths with arbitrary depth can be created.
     *
     * @param prefix non-null prefix language path determining the context in which
     *   the language is embedded.
     * @param language non-null language description.
     * @return non-null language path.
     */
    public static LanguagePath get(LanguagePath prefix, LanguageDescription<? extends TokenId> language) {
        return prefix.createEmbedded(language);
    }
    
    /**
     * Array of component language paths for this language path.
     * <br>
     * The last member of the array is <code>this</code>.
     */
    private final LanguageDescription<? extends TokenId>[] languages;
    
    /**
     * Mapping of embedded language to a weak reference to LanguagePath.
     */
    private Map<LanguageDescription<? extends TokenId>,Reference<LanguagePath>> language2path;
    
    /**
     * Cached and interned mime-path string.
     */
    private String mimePath;
    
    
    private LanguagePath(LanguagePath prefix, LanguageDescription<? extends TokenId> language) {
        int prefixSize = prefix.size();
        this.languages = allocateLanguageDescriptionArray(prefixSize + 1);
        System.arraycopy(prefix.languages, 0, this.languages, 0, prefixSize);
        this.languages[prefixSize] = language;
    }
    
    /** Build EMPTY LanguagePath */
    private LanguagePath() {
        this.languages = allocateLanguageDescriptionArray(0);
    }
    
    /**
     * Get total number of languages in this language path.
     *
     * @return >=1 number of languages contained in this language path.
     */
    public int size() {
        return languages.length;
    }
    
    /**
     * Get language description of this language path at the given index.
     * <br>
     * Index zero corresponds to the root language.
     *
     * @param index >=0 && < {@link #size()}.
     * @return non-null language at the given index.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public LanguageDescription<? extends TokenId> language(int index) {
        return languages[index];
    }

    /**
     * Check whether the language description of this language path
     * at the given index is the given language description.
     * <br>
     * Index zero corresponds to the root language.
     *
     * @param index >=0 && < {@link #size()}.
     * @return non-null language at the given index.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public boolean isLanguage(int index, LanguageDescription<? extends TokenId> language) {
        return (language(index) == language);
    }

    /**
     * Return the top-level language of this language path.
     * <br/>
     * It's equivalent to <code>language(0)</code>.
     *
     * @see #language(int)
     */
    public LanguageDescription<? extends TokenId> topLanguage() {
        return language(0);
    }
    
    /**
     * Check whether the top-level language of this language path
     * is the given language.
     *
     * @see #isLanguage(int, LanguageDescription)
     */
    public boolean isTopLanguage(LanguageDescription<? extends TokenId> language) {
        return (topLanguage() == language);
    }
    
    /**
     * Return the most inner language of this path.
     * <br/>
     * It's equivalent to <code>language(size() - 1)</code>.
     *
     * @see #language(int)
     */
    public LanguageDescription<? extends TokenId> innerLanguage() {
        return language(size() - 1);
    }
    
    /**
     * Check whether the most inner language of this language path
     * is the given language.
     *
     * @see #isLanguage(int, LanguageDescription)
     */
    public boolean isInnerLanguage(LanguageDescription<? extends TokenId> language) {
        return (innerLanguage() == language);
    }
    
    /**
     * Check whether this language path ends with the given language path.
     * <br/>
     * This may be useful for checking whether a given input contains certain language
     * (or language path) that may possibly be embedded somewhere in the input.
     *
     * @param languagePath non-null language path to be checked.
     * @return true if this language path contains the given language path
     *  at its end (applies for <code>this</code> as well).
     */
    public boolean endsWith(LanguagePath languagePath) {
        if (languagePath == this || languagePath == EMPTY)
            return true;
        int lpSize = languagePath.size();
        if (lpSize <= size()) {
            for (int i = 1; i <= lpSize; i++) {
                if (language(size() - i) != languagePath.language(lpSize - i))
                    return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Gets the path starting at the given index and ending after
     * the last language contained in this path.
     *
     * @see #subPath(int, int)
     */
    public LanguagePath subPath(int startIndex) {
        return subPath(startIndex, size());
    }

    /**
     * Gets the path starting at the given index and ending after
     * the last language contained in this path.
     *
     * @param startIndex >=0 starting index of the requested path in this path.
     * @param endIndex >startIndex index after the last item
     *  of the requested path.
     * @return non-null language path containing items between startIndex and endIndex.
     */
    public LanguagePath subPath(int startIndex, int endIndex) {
        if (startIndex < 0) {
	    throw new IndexOutOfBoundsException("startIndex=" + startIndex + " < 0"); // NOI18N
	}
	if (endIndex > size()) {
	    throw new IndexOutOfBoundsException("endIndex=" + endIndex + " > size()=" + size());
	}
	if (startIndex >= endIndex) {
	    throw new IndexOutOfBoundsException("startIndex=" + startIndex + " >= endIndex=" + endIndex);
	}
	if (startIndex == 0 && endIndex == size()) {
            return this;
        }
        LanguagePath lp = LanguagePath.get(language(startIndex++));
        while (startIndex < endIndex) {
            lp = LanguagePath.get(lp, language(startIndex++));
        }
        return lp;
    }

    /**
     * Gets the mime path equivalent of this language path. The mime path is
     * a concatenation of mime types of all the languages in this language path.
     * The mime types are separated by the '/' character.
     *
     * <p>
     * For example the language path of the java language embedded in the
     * JSP language will return 'text/x-jsp/text/x-java' when this method is called.
     * </p>
     *
     * <p>
     * The returned string path can be used in MimeLookup's operation
     * to obtain a corresponding MimePath object by using
     * <code>MimePath.parse(returned-mime-path-string)</code>.
     * </p>
     *
     * @return The mime path string.
     * @see org.netbeans.spi.lexer.LanguageHierarchy#mimeType()
     */
    public String mimePath() {
        synchronized (languages) {
            if (mimePath == null) {
                StringBuilder sb = new StringBuilder(15 * languages.length);
                for (LanguageDescription<? extends TokenId> language : languages) {
                    if (sb.length() > 0) {
                        sb.append('/');
                    }
                    sb.append(language.mimeType());
                }
                // Intern the mimePath for faster operation of MimePath.parse()
                mimePath = sb.toString().intern();
            }
            return mimePath;
        }
    }
    
    private LanguagePath createEmbedded(LanguageDescription<? extends TokenId> language) {
        if (language == null) {
            throw new IllegalArgumentException("language cannot be null");
        }
        // Attempt to retrieve from the cache first
        synchronized (languages) {
            if (language2path == null) {
                language2path = new WeakHashMap<LanguageDescription<? extends TokenId>,Reference<LanguagePath>>();
            }
            Reference<LanguagePath> lpRef = language2path.get(language);
            LanguagePath lp;
            if (lpRef == null || (lp = (LanguagePath)lpRef.get()) == null) {
                // Construct the LanguagePath
                lp = new LanguagePath(this, language);
                language2path.put(language, new SoftReference<LanguagePath>(lp));
            }
        
            return lp;
        }
    }
    
    @SuppressWarnings("unchecked")
    private LanguageDescription<? extends TokenId>[] allocateLanguageDescriptionArray(int length) {
        return (LanguageDescription<? extends TokenId>[])(new LanguageDescription[length]);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LanguagePath: size=");
        sb.append(size());
        sb.append('\n');
        for (int i = 0; i < size(); i++) {
            sb.append('[').append(i).append("]: "); // NOI18N
            sb.append(language(i)).append('\n');
        }
        return sb.toString();
    }
    
}