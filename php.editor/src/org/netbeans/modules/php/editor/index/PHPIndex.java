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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.index;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class PHPIndex {

    /** Set property to true to find ALL functions regardless of file includes */
    //private static final boolean ALL_REACHABLE = Boolean.getBoolean("javascript.findall");
    private static final boolean ALL_REACHABLE = !Boolean.getBoolean("javascript.checkincludes");
    private static String clusterUrl = null;
    private static final String CLUSTER_URL = "cluster:"; // NOI18N

    static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    private static final Set<String> TERMS_FQN = Collections.singleton(PHPIndexer.FIELD_FQN);
    private static final Set<String> TERMS_BASE = Collections.singleton(PHPIndexer.FIELD_BASE);
    private static final Set<String> TERMS_CONST = Collections.singleton(PHPIndexer.FIELD_CONST);
    private static final Set<String> TERMS_EXTEND = Collections.singleton(PHPIndexer.FIELD_EXTEND);
    private final Index index;

    /** Creates a new instance of JsIndex */
    public PHPIndex(Index index) {
        this.index = index;
    }

    public static PHPIndex get(Index index) {
        return new PHPIndex(index);
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

    static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    /** Get the FileObject corresponding to a URL returned from the index */
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

    static String getClusterUrl() {
        if (clusterUrl == null) {
            File f =
                    InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-javascript-editing.jar", null, false); // NOI18N

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

//    @SuppressWarnings("unchecked")
//    public Set<IndexedElement> getConstructors(final String name, NameKind kind,
//        Set<Index.SearchScope> scope) {
//        // TODO - search by the FIELD_CLASS thingy
//        return getUnknownFunctions(name, kind, scope, true, null, true, false);
//    }
//    
//    @SuppressWarnings("unchecked")
//    public Set<IndexedElement> getAllNames(final String name, NameKind kind,
//        Set<Index.SearchScope> scope, JsParseResult context) {
//        // TODO - search by the FIELD_CLASS thingy
//        return getUnknownFunctions(name, kind, scope, false, context, true, true);
//    }
//    
//    private String getExtends(String className, Set<Index.SearchScope> scope) {
//        final Set<SearchResult> result = new HashSet<SearchResult>();
//        search(PHPIndexer.FIELD_EXTEND, className.toLowerCase(), NameKind.CASE_INSENSITIVE_PREFIX, result, scope, TERMS_EXTEND);
//        String target = className.toLowerCase()+";";
//        for (SearchResult map : result) {
//            String[] exts = map.getValues(PHPIndexer.FIELD_EXTEND);
//            
//            if (exts != null) {
//                for (String ext : exts) {
//                    if (ext.startsWith(target)) {
//                        // Make sure it's a case match
//                        int caseIndex = target.length();
//                        int end = ext.indexOf(';', caseIndex);
//                        if (className.equals(ext.substring(caseIndex, end))) {
//                            return ext.substring(end+1);
//                        }
//                    }
//                }
//            }
//        }
//        
//        return null;
//    }
//    
//    /** Return both functions and properties matching the given prefix, of the
//     * given (possibly null) type
//     */
//    public Set<IndexedElement> getElements(String prefix, String type,
//            NameKind kind, Set<Index.SearchScope> scope, JsParseResult context) {
//        return getByFqn(prefix, type, kind, scope, false, context, true, true);
//    }

//    @SuppressWarnings("unchecked")
//    public Set<IndexedFunction> getFunctions(String name, String in, NameKind kind,
//        Set<Index.SearchScope> scope, JsParseResult context, boolean includeMethods) {
//        return (Set<IndexedFunction>)(Set)getByFqn(name, in, kind, scope, false, context, includeMethods, false);
//    }
//    
//    private Set<IndexedElement> getUnknownFunctions(String name, NameKind kind,
//        Set<Index.SearchScope> scope, boolean onlyConstructors, JsParseResult context,
//        boolean includeMethods, boolean includeProperties) {
//        
//        final Set<SearchResult> result = new HashSet<SearchResult>();
//
//        String field = PHPIndexer.FIELD_BASE;
//        Set<String> terms = TERMS_BASE;
//        
//        NameKind originalKind = kind;
//        if (kind == NameKind.EXACT_NAME) {
//            // I can't do exact searches on methods because the method
//            // entries include signatures etc. So turn this into a prefix
//            // search and then compare chopped off signatures with the name
//            kind = NameKind.PREFIX;
//        }
//        
//        String lcname = name.toLowerCase();
//        search(field, lcname, kind, result, scope, terms);
//
//        final Set<IndexedElement> elements = new HashSet<IndexedElement>();
//        String searchUrl = null;
//        if (context != null) {
//            try {
//                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
//            } catch (FileStateInvalidException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//
//        for (SearchResult map : result) {
//            String[] signatures = map.getValues(field);
//            
//            if (signatures != null) {
//                // Check if this file even applies
//                if (context != null) {
//                    String fileUrl = map.getPersistentUrl();
//                    if (searchUrl == null || !searchUrl.equals(fileUrl)) {
//                        boolean isLibrary = fileUrl.indexOf("jsstubs") != -1; // TODO - better algorithm
//                        if (!isLibrary && !isReachable(context, fileUrl)) {
//                            continue;
//                        }
//                    }
//                }
//                
//                for (String signature : signatures) {
//                    // Lucene returns some inexact matches, TODO investigate why this is necessary
//                    if ((kind == NameKind.PREFIX) && !signature.startsWith(lcname)) {
//                        continue;
//                    } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, lcname, 0, lcname.length())) {
//                        continue;
//                    } else if (kind == NameKind.CASE_INSENSITIVE_REGEXP) {
//                        int end = signature.indexOf(';');
//                        assert end != -1;
//                        String n = signature.substring(0, end);
//                        try {
//                            if (!n.matches(lcname)) {
//                                continue;
//                            }
//                        } catch (Exception e) {
//                            // Silently ignore regexp failures in the search expression
//                        }
//                    } else if (originalKind == NameKind.EXACT_NAME) {
//                        // Make sure the name matches exactly
//                        // We know that the prefix is correct from the first part of
//                        // this if clause, by the signature may have more
//                        if (((signature.length() > lcname.length()) &&
//                                (signature.charAt(lcname.length()) != ';'))) {
//                            continue;
//                        }
//                    }
//
//                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
//                    assert map != null;
//
//                    String elementName = null;
//                    int nameEndIdx = signature.indexOf(';');
//                    assert nameEndIdx != -1;
//                    elementName = signature.substring(0, nameEndIdx);
//                    nameEndIdx++;
//
//                    String funcIn = null;
//                    int inEndIdx = signature.indexOf(';', nameEndIdx);
//                    assert inEndIdx != -1;
//                    if (inEndIdx > nameEndIdx+1) {
//                        funcIn = signature.substring(nameEndIdx, inEndIdx);
//                    }
//                    inEndIdx++;
//
//                    int startCs = inEndIdx;
//                    inEndIdx = signature.indexOf(';', startCs);
//                    assert inEndIdx != -1;
//                    if (inEndIdx > startCs) {
//                        // Compute the case sensitive name
//                        elementName = signature.substring(startCs, inEndIdx);
//                        if (kind == NameKind.PREFIX && !elementName.startsWith(name)) {
//                            continue;
//                        } else if (kind == NameKind.EXACT_NAME && !elementName.equals(name)) {
//                            continue;
//                        }
//                    }
//                    inEndIdx++;
//                    
//                    // Filter out methods on other classes
//                    if (!includeMethods && (funcIn != null)) {
//                        continue;
//                    }
//                    
//                    IndexedElement element = IndexedElement.create(signature, map.getPersistentUrl(), elementName, funcIn, inEndIdx, this, false);
//                    boolean isFunction = element instanceof IndexedFunction;
//                    if (isFunction && !includeMethods) {
//                        continue;
//                    } else if (!isFunction && !includeProperties) {
//                        continue;
//                    }
//                    if (onlyConstructors && element.getKind() != ElementKind.CONSTRUCTOR) {
//                        continue;
//                    }
//                    elements.add(element);
//                }
//            }
//        }
//        
//        return elements;
//    }
    public Collection<IndexedFunction> getFunctions(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedFunction> functions = new ArrayList<IndexedFunction>();
        search(PHPIndexer.FIELD_BASE, name, kind, result, ALL_SCOPE, TERMS_BASE);

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null && isReachable(context, map.getPersistentUrl())) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_BASE);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    int firstSemicolon = signature.indexOf(";");
                    
                    String funcName = signature.substring(0, firstSemicolon);

                    IndexedFunction func = (IndexedFunction) IndexedElement.create(signature,
                            map.getPersistentUrl(), funcName, null, 0, this, false);

                    functions.add(func);
                }
            }
        }
        return functions;
    }
    
    public Collection<IndexedConstant> getConstants(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedConstant> constants = new ArrayList<IndexedConstant>();
        search(PHPIndexer.FIELD_CONST, name, kind, result, ALL_SCOPE, TERMS_BASE);

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null && isReachable(context, map.getPersistentUrl())) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_CONST);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {

                    IndexedConstant constant = new IndexedConstant(signature, null,
                            this, map.getPersistentUrl(), null, 0, ElementKind.GLOBAL);

                    constants.add(constant);
                }
            }
        }
        
        return constants;
    }

//    private Set<IndexedElement> getByFqn(String name, String type, NameKind kind,
//        Set<Index.SearchScope> scope, boolean onlyConstructors, JsParseResult context,
//        boolean includeMethods, boolean includeProperties) {
//        //assert in != null && in.length() > 0;
//        
//        final Set<SearchResult> result = new HashSet<SearchResult>();
//
//        String field = PHPIndexer.FIELD_FQN;
//        Set<String> terms = TERMS_FQN;
//        NameKind originalKind = kind;
//        if (kind == NameKind.EXACT_NAME) {
//            // I can't do exact searches on methods because the method
//            // entries include signatures etc. So turn this into a prefix
//            // search and then compare chopped off signatures with the name
//            kind = NameKind.PREFIX;
//        }
//        
//        if (kind == NameKind.CASE_INSENSITIVE_PREFIX || kind == NameKind.CASE_INSENSITIVE_REGEXP) {
//            // TODO - can I do anything about this????
//            //field = PHPIndexer.FIELD_BASE_LOWER;
//            //terms = FQN_BASE_LOWER;
//        }
//
//        final Set<IndexedElement> elements = new HashSet<IndexedElement>();
//        String searchUrl = null;
//        if (context != null) {
//            try {
//                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
//            } catch (FileStateInvalidException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//
//        Set<String> seenTypes = new HashSet<String>();
//        seenTypes.add(type);
//        boolean haveRedirected = false;
//        boolean inheriting = type == null;
//        
//        while (true) {
//        
//            String fqn;
//            if (type != null && type.length() > 0) {
//                fqn = type + "." + name;
//            } else {
//                fqn = name;
//            }
//
//            String lcfqn = fqn.toLowerCase();
//            search(field, lcfqn, kind, result, scope, terms);
//
//            for (SearchResult map : result) {
//                String[] signatures = map.getValues(field);
//
//                if (signatures != null) {
//                    // Check if this file even applies
//                    if (context != null) {
//                        String fileUrl = map.getPersistentUrl();
//                        if (searchUrl == null || !searchUrl.equals(fileUrl)) {
//                            boolean isLibrary = fileUrl.indexOf("jsstubs") != -1; // TODO - better algorithm
//                            if (!isLibrary && !isReachable(context, fileUrl)) {
//                                continue;
//                            }
//                        }
//                    }
//
//                    for (String signature : signatures) {
//                        // Lucene returns some inexact matches, TODO investigate why this is necessary
//                        if ((kind == NameKind.PREFIX) && !signature.startsWith(lcfqn)) {
//                            continue;
//                        } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, lcfqn, 0, lcfqn.length())) {
//                            continue;
//                        } else if (kind == NameKind.CASE_INSENSITIVE_REGEXP) {
//                            int end = signature.indexOf(';');
//                            assert end != -1;
//                            String n = signature.substring(0, end);
//                            try {
//                                if (!n.matches(lcfqn)) {
//                                    continue;
//                                }
//                            } catch (Exception e) {
//                                // Silently ignore regexp failures in the search expression
//                            }
//                        } else if (originalKind == NameKind.EXACT_NAME) {
//                            // Make sure the name matches exactly
//                            // We know that the prefix is correct from the first part of
//                            // this if clause, by the signature may have more
//                            if (((signature.length() > lcfqn.length()) &&
//                                    (signature.charAt(lcfqn.length()) != ';'))) {
//                                continue;
//                            }
//                        }
//
//                        // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
//                        assert map != null;
//
//                        String elementName = null;
//                        int nameEndIdx = signature.indexOf(';');
//                        assert nameEndIdx != -1;
//                        elementName = signature.substring(0, nameEndIdx);
//                        nameEndIdx++;
//
//                        String funcIn = null;
//                        int inEndIdx = signature.indexOf(';', nameEndIdx);
//                        assert inEndIdx != -1;
//                        inEndIdx++;
//
//                        int startCs = inEndIdx;
//                        inEndIdx = signature.indexOf(';', startCs);
//                        assert inEndIdx != -1;
//                        if (inEndIdx > startCs) {
//                            // Compute the case sensitive name
//                            elementName = signature.substring(startCs, inEndIdx);
//                            if (kind == NameKind.PREFIX && !elementName.startsWith(fqn)) {
//                                continue;
//                            } else if (kind == NameKind.EXACT_NAME && !elementName.equals(fqn)) {
//                                continue;
//                            }
//                        }
//                        inEndIdx++;
//
//                        int lastDot = elementName.lastIndexOf('.');
//                        IndexedElement element = null;
//                        if (name.length() < lastDot) {
//                            int nextDot = elementName.indexOf('.', fqn.length());
//                            if (nextDot != -1) {
//                                int flags = IndexedElement.decode(signature, inEndIdx, 0);
//                                ElementKind k = ElementKind.PACKAGE;
//                                // If there are no more dots after this one, it's a class, not a package
//                                int nextNextDot = elementName.indexOf('.', nextDot+1);
//                                if (nextNextDot == -1) {
//                                    k = ElementKind.CLASS;
//                                }
//                                if (type != null && type.length() > 0) {
//                                    String pkg = elementName.substring(type.length()+1, nextDot);
//                                    element = new IndexedPackage(pkg, null, this, map.getPersistentUrl(), signature, flags, k);
//                                } else {
//                                    String pkg = elementName.substring(0, nextDot);
//                                    element = new IndexedPackage(pkg, null, this, map.getPersistentUrl(), signature, flags, k);
//                                }
//                            } else {
//                                funcIn = elementName.substring(0, lastDot);
//                                elementName = elementName.substring(lastDot+1);
//                            }
//                        } else if (lastDot != -1) {
//                            funcIn = elementName.substring(0, lastDot);
//                            elementName = elementName.substring(lastDot+1);
//                        }
//                        if (element == null) {
//                            element = IndexedElement.create(signature, map.getPersistentUrl(), elementName, funcIn, inEndIdx, this, false);
//                        }
//                        boolean isFunction = element instanceof IndexedFunction;
//                        if (isFunction && !includeMethods) {
//                            continue;
//                        } else if (!isFunction && !includeProperties) {
//                            continue;
//                        }
//                        if (onlyConstructors && element.getKind() != ElementKind.CONSTRUCTOR) {
//                            continue;
//                        }
//                        if (!haveRedirected) {
//                            element.setSmart(true);
//                        }
//                        if (!inheriting) {
//                            element.setInherited(false);
//                        }
//                        elements.add(element);
//                    }
//                }
//            }
//            
//            if (type == null || "Object".equals(type)) { // NOI18N
//                break;
//            }
//            type = getExtends(type, scope);
//            if (type == null) {
//                type = "Object"; // NOI18N
//                haveRedirected = true;
//            }
//            // Prevent circularity in types
//            if (seenTypes.contains(type)) {
//                break;
//            } else {
//                seenTypes.add(type);
//            }
//            inheriting = true;
//        }
//        
//        return elements;
//    }
    /** Try to find the type of a symbol and return it */
//    public String getType(String symbol) {
//        //assert in != null && in.length() > 0;
//
//        final Set<SearchResult> result = new HashSet<SearchResult>();
//
//        String field = PHPIndexer.FIELD_FQN;
//        Set<String> terms = TERMS_BASE;
//        String lcsymbol = symbol.toLowerCase();
//        search(field, lcsymbol, NameKind.PREFIX, result, ALL_SCOPE, terms);
//
////        final Set<IndexedElement> elements = new HashSet<IndexedElement>();
////        String searchUrl = null;
////        if (context != null) {
////            try {
////                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
////            } catch (FileStateInvalidException ex) {
////                Exceptions.printStackTrace(ex);
////            }
////        }
//
//        for (SearchResult map : result) {
//            String[] signatures = map.getValues(field);
//
//            if (signatures != null) {
////                // Check if this file even applies
////                if (context != null) {
////                    String fileUrl = map.getPersistentUrl();
////                    if (searchUrl == null || !searchUrl.equals(fileUrl)) {
////                        boolean isLibrary = fileUrl.indexOf("jsstubs") != -1; // TODO - better algorithm
////                        if (!isLibrary && !isReachable(context, fileUrl)) {
////                            continue;
////                        }
////                    }
////                }
//
//                for (String signature : signatures) {
//                    // Lucene returns some inexact matches, TODO investigate why this is necessary
//                    // Make sure the name matches exactly
//                    // We know that the prefix is correct from the first part of
//                    // this if clause, by the signature may have more
//                    if (((signature.length() > lcsymbol.length()) &&
//                            (signature.charAt(lcsymbol.length()) != ';'))) {
//                        continue;
//                    }
//
//                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
//                    assert map != null;
//
//                    String elementName = null;
//                    int nameEndIdx = signature.indexOf(';');
//                    assert nameEndIdx != -1;
//                    elementName = signature.substring(0, nameEndIdx);
//                    if (!elementName.startsWith(symbol)) {
//                        continue;
//                    }
//                    nameEndIdx++;
//
//                    String funcIn = null;
//                    int inEndIdx = signature.indexOf(';', nameEndIdx);
//                    assert inEndIdx != -1;
//                    if (inEndIdx > nameEndIdx + 1) {
//                        funcIn = signature.substring(nameEndIdx, inEndIdx);
//                    }
//                    inEndIdx++;
//
//                    int startCs = inEndIdx;
//                    inEndIdx = signature.indexOf(';', startCs);
//                    assert inEndIdx != -1;
////                    if (inEndIdx > startCs) {
////                        // Compute the case sensitive name
////                        elementName = signature.substring(startCs, inEndIdx);
////                    }
//                    inEndIdx++;
//
//                    // Filter out methods on other classes
////                    if (!includeMethods && (funcIn != null)) {
////                        continue;
////                    } else if (in != null && (funcIn == null || !funcIn.equals(in))) {
////                        continue;
////                    }
//
//                    IndexedElement element = IndexedElement.create(signature, map.getPersistentUrl(), elementName, funcIn, inEndIdx, this, false);
////                    boolean isFunction = element instanceof IndexedFunction;
////                    if (isFunction && !includeMethods) {
////                        continue;
////                    } else if (!isFunction && !includeProperties) {
////                        continue;
////                    }
////                    if (onlyConstructors && element.getKind() != ElementKind.CONSTRUCTOR) {
////                        continue;
////                    }
////                    elements.add(element);
//
//                    String type = element.getType();
//                    if (type != null) {
//                        return type;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }

    /** 
     * Decide whether the given url is included from the current compilation
     * context.
     * This will typically return true for all library files, and false for
     * all source level files unless that file is reachable through include-mechanisms
     * from the current file.
     */
    public boolean isReachable(PHPParseResult result, String url) {
        String processedFileURL = null;
        
        try {
            processedFileURL = result.getFile().getFileObject().getURL().toExternalForm();
            
            if (url.equals(processedFileURL)){
                return true;
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        Collection<String> includes = new ArrayList<String>();
        
        for (Statement statement : result.getProgram().getStatements()) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = (ExpressionStatement) statement;
                if (expressionStatement.getExpression() instanceof Include){
                    Include include = (Include)expressionStatement.getExpression();
                    if (include.getExpression() instanceof Scalar){
                        Scalar scalar = (Scalar)include.getExpression();
                        includes.add(scalar.getStringValue());
                    }
                }
            }
        }
        
        for (String includeInQuotes : includes){
            // start of provisional code
            //TODO: a more sophisticated check here,
            String incl = dequote(includeInQuotes);
            assert processedFileURL.startsWith("file:");
            String processedFileAbsPath = processedFileURL.substring("file:".length());
            String includeURL = resolveRelativeURL(processedFileAbsPath, incl);
            
            if (url.equals("file:" + includeURL)){
                return true;
            }
            
            // end of provisional code
        }

        return false;
    }
    
    static String dequote(String string){
        assert string.length() >= 2;
        assert string.startsWith("\"") || string.startsWith("'");
        assert string.endsWith("\"") || string.endsWith("'");
        return string.substring(1, string.length() - 1);
    }
    
    // copied from JspUtils
    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
    *  @param relativeTo url to which the relative URL is related. Treated as directory iff
    *    ends with '/'
    *  @param url the relative URL by RFC 2396
    *  @exception IllegalArgumentException if url is not absolute and relativeTo 
    * can not be related to, or if url is intended to be a directory
    */
    static String resolveRelativeURL(String relativeTo, String url) {
        //System.out.println("- resolving " + url + " relative to " + relativeTo);
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        }
        else {
            // canonize relativeTo
            if ((relativeTo == null) || (!relativeTo.startsWith("/"))) // NOI18N
                throw new IllegalArgumentException();
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1)
                throw new IllegalArgumentException();
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                    result = result + "/"; // NOI18N
            }
            else
                if (tok.equals("")) // NOI18N
                    ;  // do nohing
                else
                    if (tok.equals(".")) // NOI18N
                        ;  // do nohing
                    else
                        if (tok.equals("..")) { // NOI18N
                            String withoutSlash = result.substring(0, result.length() - 1);
                            int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                            if (ls != -1)
                                result = withoutSlash.substring(0, ls + 1);
                        }
                        else {
                            // some file
                            result = result + tok;
                        }
            //System.out.println("result : " + result); // NOI18N
        }
        //System.out.println("- resolved to " + result);
        return result;
    }
}
