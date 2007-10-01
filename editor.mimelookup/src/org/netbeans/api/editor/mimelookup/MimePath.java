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

package org.netbeans.api.editor.mimelookup;

import java.lang.ref.Reference;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.modules.editor.mimelookup.MimePathLookup;

/**
 * The mime path is a concatenation of one or more mime types. The purpose of
 * a mime path is to describe the fact that a document of a certain mime type
 * can contain fragments of another document with a different mime type. The fragment
 * and its mime type is refered to as an embedded document and an embedded mime
 * type respectively. 
 *
 * <p>In order to fully understand the scale of the problem the mime path
 * is trying to describe you should consider two things. First a document can
 * contain several different embedded fragments each of a different
 * mime type. Second, each embeded fragment itself can possibly contain one or
 * more other embedded fragments and this nesting can in theory go indefinitely
 * deep.
 *
 * <p>In reality the nesting probably will not be very deep. As an example of a
 * document containing an embedded fragment of another document of the different
 * mime type you could imagine a JSP page containing a Java scriplet. The main
 * document is the JSP page of the 'text/x-jsp' mime type, which includes a fragment
 * of Java source code of the 'text/x-java' mime type.
 *
 * <p>The mime path comes handy when we want to distinguish between the ordinary
 * 'text/x-java' mime type and the 'text/x-java' mime type embedded in the JSP
 * page, because both of those 'text/x-java' mime types will have a different
 * mime path. The ordinary 'text/x-java' mime type has a mime path consisting
 * of just one mime type - 'text/x-java'. The 'text/x-java' mime type embeded in
 * the JSP page, however, has a mime path comprised from two mime types
 * 'text/x-jsp' and 'text/x-java'. The order of mime types in a mime path is
 * obviously very important, because it describes how the mime types are embedded.
 *
 * <p>The mime path can be represented as a <code>String</code> simply by
 * concatenating all its mime types separated by the '/' character. Since
 * mime types always contain one and only one '/' character it is clear which
 * '/' character belongs to a mime type and which is the mime path separator.
 *
 * <p>In the above example the mime path of the 'text/x-java' mime type embedded
 * in the 'text/x-jsp' mime type can be represented as 'text/x-jsp/text/x-java'.
 *
 * <p class="nonnormative">For some languages it is not uncommon to allow embedding of itself. For example
 * in Ruby it is allowed to use Ruby code within strings and Ruby will
 * evaluate this code when evaluating the value of the strings. Depending on the
 * implementation of a lexer there can be tokens with <code>MimePath</code> that
 * contains several consecutive mime types that are the same.
 * 
 * <p>The format of a valid mime type string is described in
 * <a href="http://tools.ietf.org/html/rfc4288#section-4.2">RFC 4288</a>.
 * <code>MimePath</code> performs internall checks according to this specification.
 * 
 * <p><b>Identity:</b> By definition two <code>MimePath</code> instances are equal
 * if they represent the same string mime path. The implementation guarantees
 * that by caching and reusing instances of the <code>MimePath</code> that it
 * creates. The <code>MimePath</code> instances can be used as keys in maps.
 *
 * <p><b>Lifecycle:</b> Although the instances of <code>MimePath</code> are
 * internally cached and should survive for certain time without being referenced
 * from outside of the MimePath API, clients are strongly encouraged to hold
 * a reference to the <code>MimePath</code> they obtained throughout the whole
 * lifecycle of their component. For example an opened java editor with a document
 * should keep its instance of the 'text/x-java' <code>MimePath</code> for the
 * whole time the editor is open.
 *
 * @author Miloslav Metelka, Vita Stejskal
 * @see MimeLookup
 * @see <a href="http://tools.ietf.org/html/rfc4288#section-4.2">RFC 4288</a>
 */
public final class MimePath {
    
    /**
     * The root of all mime paths. The empty mime path does not refer to any
     * mime type.
     */
    public static final MimePath EMPTY = new MimePath();

    /** Internal lock to manage the cache maps. */
    private static final Object LOCK = new Object();

    /** The List of Recently Used mime paths. */
    private static final ArrayList<MimePath> LRU = new ArrayList<MimePath>();

    /** The maximum size of the List of Recently Used mime paths.
    /* package */ static final int MAX_LRU_SIZE = 3;

    private static final Pattern REG_NAME_PATTERN = Pattern.compile("^[[\\p{Alnum}][!#$&.+\\-^_]]{1,127}$"); //NOI18N

    private static final Set<String> WELL_KNOWN_TYPES = new HashSet<String>(Arrays.asList(
        "application", //NOI18N
        "audio", //NOI18N
        "content", //NOI18N   for content/unknown mime type
        "image", //NOI18N
        "message", //NOI18N
        "model", //NOI18N
        "multipart", //NOI18N
        "text", //NOI18N
        "video" //NOI18N
    ));
    
    private static final Map<String,Reference<MimePath>> string2mimePath = new HashMap<String,Reference<MimePath>>();
    
    /**
     * Gets the mime path for the given mime type. The returned <code>MimePath</code>
     * will contain exactly one element and it will be the mime type passed in
     * as the parameter.
     *
     * @param mimeType The mime type to get the mime path for. If <code>null</code>
     * or empty string is passed in the <code>EMPTY</code> mime path will be
     * returned.
     *
     * @return The <code>MimePath</code> for the given mime type or
     * <code>MimePath.EMPTY</code> if the mime type is <code>null</code> or empty
     * string.
     */
    public static MimePath get(String mimeType) {
        if (mimeType == null || mimeType.length() == 0){
            return EMPTY;
        } else {
            return get(EMPTY, mimeType);
        }
    }
    
    /**
     * Gets the mime path corresponding to a mime type embedded in another
     * mime type. The embedding mime type is described in form of a mime path
     * passed in as the <code>prefix</code> parameter.
     *
     * <p>For example for a java scriplet embedded in a jsp page the <code>prefix</code> would 
     * be the mime path 'text/x-jsp' and <code>mimeType</code> would be 'text/x-java'.
     * The method will return the 'text/x-jsp/text/x-java' mime path.
     *
     *
     * @param prefix The mime path determining the mime type that embedds the mime
     * type passed in in the second parameter. It can be {@link #EMPTY} in which
     * case the call will be equivalent to calling <code>get(mimeType)</code> method.
     * @param mimeType The mime type that is embedded in the mime type determined
     * by the <code>prefix</code> mime path.
     *
     * @return The mime path representing the embedded mime type.
     */
    public static MimePath get(MimePath prefix, String mimeType) {
        if (!validate(mimeType)) {
            throw new IllegalArgumentException("Invalid mimeType=\"" + mimeType + "\""); //NOI18N
        }
        
        return prefix.getEmbedded(mimeType);
    }
    
    /**
     * Parses a mime path string and returns its <code>MimePath</code> representation.
     *
     * <p>The format of a mime path string representation is a string of mime
     * type components comprising the mime path separated by the '/' character.
     * For example a mime path representing the 'text/x-java' mime type embedded
     * in the 'text/x-jsp' mime type can be represented as the following string -
     * 'text/x-jsp/text/x-java'.
     *
     * <p>The mime path string can be an empty string, which represents the
     * {@link #EMPTY} mime path. By definition all valid mime paths except of
     * the empty one have to contain odd number of '/' characters.
     *
     * @param path The mime path string representation. 
     *
     * @return non-null mime-path corresponding to the given string path.
     */
    public static MimePath parse(String path) {
        assert path != null : "path cannot be null"; // NOI18N
        
        synchronized (string2mimePath) {
            Reference<MimePath> mpRef = string2mimePath.get(path);
            MimePath mimePath = mpRef != null ? mpRef.get() : null;
            
            if (mimePath != null) {
                return mimePath;
            }
        }

        // Parse the path
        Object o = parseImpl(path, false);
        if (!(o instanceof MimePath)) {
            throw new IllegalArgumentException((String) o);
        }
        
        synchronized (string2mimePath) {
            MimePath mimePath = (MimePath) o;
            
            // Intern the path since the language path's string path is also interned
            // and thus they can be matched by identity
            string2mimePath.put(path.intern(), new WeakReference<MimePath>(mimePath));
            
            return mimePath;
        }
    }

    /**
     * Validates components of a mime type. Each mime types is compound from
     * two components - <i>type</i> and <i>subtype</i>. There are rules that
     * both components must obey. For details see 
     * <a href="http://tools.ietf.org/html/rfc4288#section-4.2">RFC 4288</a>.
     * 
     * @param type The type component of a mime type to validate. If <code>null</code>
     *   the type component will not be validated.
     * @param subtype The subtype component of a mime type to validate. If <code>null</code>
     *   the subtype component will not be validated.
     * 
     * @return <code>true</code> if non-</code>null</code> components passed in
     *   are valid mime type components, otherwise <code>false</code>.
     * @since 1.7
     */
    public static boolean validate(CharSequence type, CharSequence subtype) {
        if (type != null) {
            // HACK: 
            if (startsWith(type, "test")) { //NOI18N
                for(int i = 4; i < type.length(); i++) {
                    if (type.charAt(i) == '_') { //NOI18N
                        type = type.subSequence(i + 1, type.length());
                        break;
                    }
                }
            }
            
            if (!WELL_KNOWN_TYPES.contains(type.toString())) {
                return false;
            }
        }

        if (subtype != null) {
            if (!REG_NAME_PATTERN.matcher(subtype).matches()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates a path to check if it's a valid mime path. If this method
     * returns <code>true</code> the path is a valid mime path string and can
     * be used in the <code>MimePath.parse()</code> method.
     * 
     * @param path The path string to validate.
     * 
     * @return <code>true</code> if the path string is a valid mime path.
     * @since 1.7
     */
    public static boolean validate(CharSequence path) {
        // parseImpl will return error string if parsing fails
        return !(parseImpl(path, true) instanceof String);
    }
    
    /**
     * Array of component mime paths for this mime path.
     * <br>
     * The last member of the array is <code>this</code>.
     */
    private final MimePath[] mimePaths;
    
    /**
     * Complete string path of this mimePath.
     */
    private final String path;

    /**
     * Mime type string represented by this mime path component.
     */
    private final String mimeType;
    
    /**
     * Mapping of embedded mimeType to a weak reference to mimePath.
     */
    private Map<String, SoftReference<MimePath>> mimeType2mimePathRef;

    /**
     * The lookup with objects registered for this mime path.
     */
    private MimePathLookup lookup;
    
    /**
     * Synchronization lock for creation of the mime path lookup.
     */
    private final String LOOKUP_LOCK = new String("MimePath.LOOKUP_LOCK"); //NOI18N
    
    private MimePath(MimePath prefix, String mimeType) {
        int prefixSize = prefix.size();
        this.mimePaths = new MimePath[prefixSize + 1];
        System.arraycopy(prefix.mimePaths, 0, this.mimePaths, 0, prefixSize);
        this.mimePaths[prefixSize] = this;
        String prefixPath = prefix.path;
        this.path = (prefixPath != null && prefixPath.length() > 0 ) ? 
            (prefixPath + '/' + mimeType).intern() : //NOI18N
            mimeType.intern();
        this.mimeType = mimeType;
    }
    
    /** Build EMPTY mimePath */
    private MimePath() {
        this.mimePaths = new MimePath[0];
        this.path = ""; //NOI18N
        this.mimeType = ""; //NOI18N
    }
    
    /**
     * Get string path represented by this mime-path.
     * <br/>
     * For example <code>"text/x-jsp/text/x-java"</code>.
     *
     * @return non-null string path.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Get total number of mime-types in the mime-path.
     * <br>
     * {@link #EMPTY} mime-path has zero size.
     * <br>
     * <code>"text/x-jsp/text/x-java"</code> has size 2.
     *
     * @return >=0 number of mime-types contained in this mime-path.
     */
    public int size() {
        return mimePaths.length;
    }
    
    /**
     * Get mime type of this mime-path at the given index.
     * <br>
     * Index zero corresponds to the root mime-type.
     * <br>
     * For <code>"text/x-jsp/text/x-java"</code> 
     * <code>getMimeType(0)</code> returns <code>"text/x-jsp"</code>
     * and <code>getMimeType(1)</code> returns <code>"text/x-java"</code>.
     *
     * @param index >=0 && < {@link #size()}.
     * @return non-null mime-type at the given index.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public String getMimeType(int index) {
        return mimePaths[index].mimeType;
    }
    
    /**
     * Return prefix mime-path with the given number of mime-type components
     * ranging from zero till the size of this mime-path.
     *
     * @param size >=0 && <= {@link #size()}.
     *  <br>
     *  For zero size the {@link #EMPTY} will be returned.
     *  <br>
     *  For <code>size()</code> <code>this</code> will be returned.
     * @return non-null mime-type of the given size.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public MimePath getPrefix(int size) {
        return (size == 0)
            ? EMPTY
            : mimePaths[size - 1];
    }

    private MimePath getEmbedded(String mimeType) {
        // Attempt to retrieve from the cache first
        // It has also an advantage that the mime-type does not need
        // to be tested for correctness
        synchronized (LOCK) {
            if (mimeType2mimePathRef == null) {
                mimeType2mimePathRef = new HashMap<String, SoftReference<MimePath>>();
            }
            Reference mpRef = mimeType2mimePathRef.get(mimeType);
            MimePath mimePath;
            if (mpRef == null || (mimePath = (MimePath)mpRef.get()) == null) {
                // Construct the mimePath
                mimePath = new MimePath(this, mimeType);
                mimeType2mimePathRef.put(mimeType, new SoftReference<MimePath>(mimePath));

                // Hard reference the last few MimePaths created.
                LRU.add(0, mimePath);
                if (LRU.size() > MAX_LRU_SIZE) {
                    LRU.remove(LRU.size() - 1);
                }
            }
        
            return mimePath;
        }
    }
    
    private static Object parseImpl(CharSequence path, boolean validateOnly) {
        MimePath mimePath = EMPTY;
        int pathLen = path.length();
        int startIndex = 0;
        while (true) {
            int index = startIndex;
            int slashIndex = -1;
            // Search for first slash
            while (index < pathLen) {
                if (path.charAt(index) == '/') { //NOI18N
                    slashIndex = index;
                    break; // first slash found
                }
                index++;
            }
            if (slashIndex == -1) { // no slash found
                if (index != startIndex) {
                    return "mimeType '" + path.subSequence(startIndex, path.length()) + //NOI18N
                            "' does not contain '/'."; // NOI18N
                }
                // Empty mimeType
                break;
            }
            index++; // move after slash
            while (index < pathLen) {
                if (path.charAt(index) == '/') { //NOI18N
                    if (index == slashIndex + 1) { // empty second part of mimeType
                        return "Two successive slashes in '" +  //NOI18N
                                path.subSequence(startIndex, path.length()) + "'"; // NOI18N
                    }
                    break;
                }
                index++;
            }
            if (index == slashIndex + 1) { // nothing after first slash
                return "Empty string after '/' in '" +  //NOI18N
                        path.subSequence(startIndex, path.length()) + "'"; // NOI18N
            }
            
            // Mime type found, validate
            if (!validate(path.subSequence(startIndex, slashIndex), 
                          path.subSequence(slashIndex + 1, index))
            ) {
                return "Invalid mimeType=\"" + path.subSequence(startIndex, index) + "\""; //NOI18N
            }
            
            if (!validateOnly) {
                String mimeType = path.subSequence(startIndex, index).toString();
                mimePath = mimePath.getEmbedded(mimeType);
            }
            
            startIndex = index + 1; // after slash or after end of path
        }
        return mimePath;
    }

    /**
     * Gets the <code>MimePathLookup</code> for the given mime path. The lookups
     * are cached and reused.
     *
     * @param The mime path to get the lookup for.
     *
     * @return The mime path specific lookup.
     */
    /* package */ MimePathLookup getLookup() {
        synchronized (LOOKUP_LOCK) {
            if (lookup == null) {
                lookup = new MimePathLookup(this);
            }
            return lookup;
        }
    }

    public @Override String toString() {
        return "MimePath[" + path + "]"; // NOI18N
    }

    private static boolean startsWith(CharSequence sequence, CharSequence subSequence) {
        if (sequence.length() < subSequence.length()) {
            return false;
        }
        
        for(int i = 0; i < subSequence.length(); i++) {
            if (sequence.charAt(i) != subSequence.charAt(i)) {
                return false;
            }
        }
        
        return true;
    }
}
