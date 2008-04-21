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
package org.netbeans.modules.groovy.editor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.modules.groovy.editor.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.elements.IndexedElement;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * 
 * @author Tor Norbye
 * @author Martin Adamek
 */
public final class GroovyIndex {

    public static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    public static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    
    private static final String CLUSTER_URL = "cluster:"; // NOI18N

    private static String clusterUrl = null;
    private final Index index;

    public GroovyIndex(Index index) {
        this.index = index;
    }
    
    /**
     * Return the full set of classes that match the given name.
     *
     * @param name The name of the class - possibly a fqn like file.Stat, or just a class
     *   name like Stat, or just a prefix like St.
     * @param kind Whether we want the exact name, or whether we're searching by a prefix.
     * @param includeAll If true, return multiple IndexedClasses for the same logical
     *   class, one for each declaration point.
     */
    public Set<IndexedClass> getClasses(String name, final NameKind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules, Set<Index.SearchScope> scope,
        Set<String> uniqueClasses) {
        String classFqn = null;

        if (name != null) {
            if (name.endsWith(".")) {
                // User has typed something like "Test." and wants completion on
                // for something like Test.Unit
                classFqn = name.substring(0, name.length() - 1);
                name = "";
            }
        }

        final Set<SearchResult> result = new HashSet<SearchResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field;

        switch (kind) {
        case EXACT_NAME:
        case PREFIX:
        case CAMEL_CASE:
        case REGEXP:
            field = GroovyIndexer.CLASS_NAME;

            break;

        case CASE_INSENSITIVE_PREFIX:
        case CASE_INSENSITIVE_REGEXP:
            field = GroovyIndexer.CASE_INSENSITIVE_CLASS_NAME;

            break;

        default:
            throw new UnsupportedOperationException(kind.toString());
        }

        search(field, name, kind, result, scope, null);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        if (includeAll) {
            uniqueClasses = null;
        } else if (uniqueClasses == null) {
            uniqueClasses = new HashSet<String>();
        }

        final Set<IndexedClass> classes = new HashSet<IndexedClass>();

        for (SearchResult map : result) {
            String clz = map.getValue(GroovyIndexer.CLASS_NAME);

            if (clz == null) {
                // It's probably a module
                // XXX I need to handle this... for now punt
                continue;
            }

            // Lucene returns some inexact matches, TODO investigate why this is necessary
            if ((kind == NameKind.PREFIX) && !clz.startsWith(name)) {
                continue;
            } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !clz.regionMatches(true, 0, name, 0, name.length())) {
                continue;
            }

            if (classFqn != null) {
                if (kind == NameKind.CASE_INSENSITIVE_PREFIX ||
                        kind == NameKind.CASE_INSENSITIVE_REGEXP) {
                    if (!classFqn.equalsIgnoreCase(map.getValue(GroovyIndexer.IN))) {
                        continue;
                    }
                } else if (kind == NameKind.CAMEL_CASE) {
                    String in = map.getValue(GroovyIndexer.IN);
                    if (in != null) {
                        // Superslow, make faster 
                        StringBuilder sb = new StringBuilder();
//                        String prefix = null;
                        int lastIndex = 0;
                        int idx;
                        do {

                            int nextUpper = -1;
                            for( int i = lastIndex+1; i < classFqn.length(); i++ ) {
                                if ( Character.isUpperCase(classFqn.charAt(i)) ) {
                                    nextUpper = i;
                                    break;
                                }
                            }
                            idx = nextUpper;
                            String token = classFqn.substring(lastIndex, idx == -1 ? classFqn.length(): idx);
//                            if ( lastIndex == 0 ) {
//                                prefix = token;
//                            }
                            sb.append(token); 
                            // TODO - add in Ruby chars here?
                            sb.append( idx != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N         
                            lastIndex = idx;
                        }
                        while(idx != -1);

                        final Pattern pattern = Pattern.compile(sb.toString());
                        if (!pattern.matcher(in).matches()) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (!classFqn.equals(map.getValue(GroovyIndexer.IN))) {
                        continue;
                    }
                }
            }

            String attrs = map.getValue(GroovyIndexer.CLASS_ATTRS);
            boolean isClass = true;
            if (attrs != null) {
                int flags = IndexedElement.stringToFlag(attrs, 0);
                isClass = (flags & IndexedClass.MODULE) == 0;
                
            }

            if (skipClasses && isClass) {
                continue;
            }

            if (skipModules && !isClass) {
                continue;
            }

            String fqn = map.getValue(GroovyIndexer.FQN_NAME);

            // Only return a single instance for this signature
            if (!includeAll) {
                if (!uniqueClasses.contains(fqn)) { // use a map to point right to the class
                    uniqueClasses.add(fqn);
                }
            }

            classes.add(createClass(fqn, clz, map));
        }

        return classes;
    }
    
    private IndexedClass createClass(String fqn, String clz, SearchResult map) {
        String require = map.getValue(GroovyIndexer.REQUIRE);

        // TODO - how do I determine -which- file to associate with the file?
        // Perhaps the one that defines initialize() ?
        String fileUrl = map.getPersistentUrl();

        if (clz == null) {
            clz = map.getValue(GroovyIndexer.CLASS_NAME);
        }

        String attrs = map.getValue(GroovyIndexer.CLASS_ATTRS);
        
        int flags = 0;
        if (attrs != null) {
            flags = IndexedElement.stringToFlag(attrs, 0);
        }

        IndexedClass c =
            IndexedClass.create(this, clz, fqn, fileUrl, require, attrs, flags);

        return c;
    }

    public static FileObject getFileObject(String url) {
        try {
            if (url.startsWith(CLUSTER_URL)) {
                url = getClusterUrl() + url.substring(CLUSTER_URL.length()); // NOI18N
            }

            return URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }

        return null;
    }

    private boolean search(String key, String name, NameKind kind, Set<SearchResult> result,
        Set<SearchScope> scope, Set<String> terms) {
        try {
            index.search(key, name, kind, scope, result, terms);

            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        }
    }

    public static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    static String getClusterUrl() {
        if (clusterUrl == null) {
            File f =
                InstalledFileLocator.getDefault()
                                    .locate("modules/org-netbeans-modules-groovy-editor.jar", null, false); // NOI18N

            if (f == null) {
                throw new RuntimeException("Can't find cluster");
            }

            f = new File(f.getParentFile().getParentFile().getAbsolutePath());

            try {
                f = f.getCanonicalFile();
                clusterUrl = f.toURI().toURL().toExternalForm();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        return clusterUrl;
    }
    
}
