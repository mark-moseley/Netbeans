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
package org.netbeans.modules.groovy.editor.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.groovy.editor.api.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.api.elements.IndexedElement;
import org.netbeans.modules.groovy.editor.api.elements.IndexedField;
import org.netbeans.modules.groovy.editor.api.elements.IndexedMethod;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * 
 * @author Tor Norbye
 * @author Martin Adamek
 */
public final class GroovyIndex {

    private static final Logger LOG = Logger.getLogger(GroovyIndex.class.getName());

    private static final GroovyIndex EMPTY = new GroovyIndex(null);
    
    private static final String CLUSTER_URL = "cluster:"; // NOI18N

    private static String clusterUrl = null;

    private final QuerySupport querySupport;

    /** Creates a new instance of JsIndex */
    private GroovyIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

    public static GroovyIndex get(Collection<FileObject> roots) {
        try {
            return new GroovyIndex(QuerySupport.forRoots(
                    GroovyIndexer.Factory.NAME,
                    GroovyIndexer.Factory.VERSION,
                    roots.toArray(new FileObject[roots.size()])));
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return EMPTY;
        }
    }
    
    public Set<IndexedClass> getClasses(String name, final QuerySupport.Kind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules) {
        return getClasses(name, kind, includeAll, skipClasses, skipModules, null);
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
    public Set<IndexedClass> getClasses(String name, final QuerySupport.Kind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules, Set<String> uniqueClasses) {
        String classFqn = null;

        if (name != null) {
            if (name.endsWith(".")) {
                // User has typed something like "Test." and wants completion on
                // for something like Test.Unit
                classFqn = name.substring(0, name.length() - 1);
                name = "";
            }
        }

        final Set<IndexResult> result = new HashSet<IndexResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field;

        switch (kind) {
        case EXACT:
            field = GroovyIndexer.FQN_NAME;
            break;
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

        search(field, name, kind, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        if (includeAll) {
            uniqueClasses = null;
        } else if (uniqueClasses == null) {
            uniqueClasses = new HashSet<String>();
        }

        final Set<IndexedClass> classes = new HashSet<IndexedClass>();

        for (IndexResult map : result) {
            String simpleName = map.getValue(GroovyIndexer.CLASS_NAME);

            if (simpleName == null) {
                // It's probably a module
                // XXX I need to handle this... for now punt
                continue;
            }

            // Lucene returns some inexact matches, TODO investigate why this is necessary
            if ((kind == QuerySupport.Kind.PREFIX) && !simpleName.startsWith(name)) {
                continue;
            } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !simpleName.regionMatches(true, 0, name, 0, name.length())) {
                continue;
            }

            if (classFqn != null) {
                if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX ||
                        kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                    if (!classFqn.equalsIgnoreCase(map.getValue(GroovyIndexer.IN))) {
                        continue;
                    }
                } else if (kind == QuerySupport.Kind.CAMEL_CASE) {
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

            classes.add(createClass(fqn, simpleName, map));
        }

        return classes;
    }
    
    /**
     * Return a set of methods that match the given name prefix, and are in the given
     * class and module. If no class is specified, match methods across all classes.
     * Note that inherited methods are not checked. If you want to match inherited methods
     * you must call this method on each superclass as well as the mixin modules.
     */
    @SuppressWarnings("fallthrough")
    public Set<IndexedMethod> getMethods(final String name, final String clz, QuerySupport.Kind kind) {
        boolean inherited = clz == null;

        //    public void searchByCriteria(final String name, final ClassIndex.NameKind kind, /*final ResultConvertor<T> convertor,*/ final Set<String> result) throws IOException {
        final Set<IndexResult> result = new HashSet<IndexResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field = GroovyIndexer.METHOD_NAME;
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }

        // No point in doing case insensitive searches on method names because
        // method names in Ruby are always case insensitive anyway
        //            case CASE_INSENSITIVE_PREFIX:
        //            case CASE_INSENSITIVE_REGEXP:
        //                field = RubyIndexer.FIELD_CASE_INSENSITIVE_METHOD_NAME;
        //                break;

        search(field, name, kind, result);

        //return Collections.unmodifiableSet(result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<IndexedMethod> methods = new HashSet<IndexedMethod>();

        for (IndexResult map : result) {
            if (clz != null) {
                String fqn = map.getValue(GroovyIndexer.FQN_NAME);

                if (!(clz.equals(fqn))) {
                    continue;
                }
            }

            String[] signatures = map.getValues(GroovyIndexer.METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == QuerySupport.Kind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                        int len = signature.length();
                        int end = signature.indexOf('(');
                        if (end == -1) {
                            end = signature.indexOf(';');
                            if (end == -1) {
                                end = len;
                            }
                        }
                        String n = end != len ? signature.substring(0, end) : signature;
                        try {
                            if (!n.matches(name)) {
                                continue;
                            }
                        } catch (Exception e) {
                            // Silently ignore regexp failures in the search expression
                        }
                    } else if (originalKind == QuerySupport.Kind.EXACT) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if (((signature.length() > name.length()) &&
                                (signature.charAt(name.length()) != '(')) &&
                                (signature.charAt(name.length()) != ';')) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    methods.add(createMethod(signature, map, inherited));
                }
            }

// XXX looks like copied from Ruby - getters and setters for properties are already indexed as methods
//            String[] attributes = map.getValues(GroovyIndexer.ATTRIBUTE_NAME);
//
//            if (attributes != null) {
//                for (String signature : attributes) {
//                    // Skip weird methods... Think harder about this
//                    if (((name == null) || (name.length() == 0)) &&
//                            !Character.isLowerCase(signature.charAt(0))) {
//                        continue;
//                    }
//
//                    // Lucene returns some inexact matches, TODO investigate why this is necessary
//                    if (kind == NameKind.PREFIX && !signature.startsWith(name)) {
//                        continue;
//                    } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
//                        continue;
//                    } else if (kind == NameKind.CASE_INSENSITIVE_REGEXP && !signature.matches(name)) {
//                        continue;
//                    } else if (originalKind == NameKind.EXACT_NAME) {
//                        // Make sure the name matches exactly
//                        // We know that the prefix is correct from the first part of
//                        // this if clause, by the signature may have more
//                        if (((signature.length() > name.length()) &&
//                                //(signature.charAt(name.length()) != '(')) &&
//                                (signature.charAt(name.length()) != ';'))) {
//                            continue;
//                        }
//                    }
//
//                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
//                    assert map != null;
//                    // Create method for the attribute
//                    methods.add(createMethod(signature, map, inherited));
//                }
//            }

        }

        return methods;
    }


    public Set<IndexedField> getFields(final String name, final String clz, QuerySupport.Kind kind) {

        boolean inherited = clz == null;

        //    public void searchByCriteria(final String name, final ClassIndex.NameKind kind, /*final ResultConvertor<T> convertor,*/ final Set<String> result) throws IOException {
        final Set<IndexResult> result = new HashSet<IndexResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field = GroovyIndexer.FIELD_NAME;
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }

        // No point in doing case insensitive searches on method names because
        // method names in Ruby are always case insensitive anyway
        //            case CASE_INSENSITIVE_PREFIX:
        //            case CASE_INSENSITIVE_REGEXP:
        //                field = RubyIndexer.FIELD_CASE_INSENSITIVE_METHOD_NAME;
        //                break;

        search(field, name, kind, result);

        //return Collections.unmodifiableSet(result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<IndexedField> fields = new HashSet<IndexedField>();

        for (IndexResult map : result) {
            if (clz != null) {
                String fqn = map.getValue(GroovyIndexer.FQN_NAME);

                if (!(clz.equals(fqn))) {
                    continue;
                }
            }

            String[] signatures = map.getValues(GroovyIndexer.FIELD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == QuerySupport.Kind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                        int len = signature.length();
                        int end = signature.indexOf(';');
                        if (end == -1) {
                            end = len;
                        }

                        String n = end != len ? signature.substring(0, end) : signature;
                        try {
                            if (!n.matches(name)) {
                                continue;
                            }
                        } catch (Exception e) {
                            // Silently ignore regexp failures in the search expression
                        }
                    } else if (originalKind == QuerySupport.Kind.EXACT) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if ((signature.length() > name.length()) &&
                                (signature.charAt(name.length()) != ';')) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    fields.add(createField(signature, map, inherited));
                }
            }
        }

        return fields;
    }

    /**
     * Get the set of inherited (through super classes and mixins) for the given fully qualified class name.
     * @param classFqn FQN: module1.module2.moduleN.class
     * @param prefix If kind is NameKind.PREFIX/CASE_INSENSITIVE_PREFIX, a prefix to filter methods by. Else,
     *    if kind is NameKind.EXACT_NAME filter methods by the exact name.
     * @param kind Whether the prefix field should be taken as a prefix or a whole name
     */
    public Set<IndexedMethod> getInheritedMethods(String classFqn, String prefix, QuerySupport.Kind kind) {
        boolean haveRedirected = false;

        //String field = RubyIndexer.FIELD_FQN_NAME;
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        Set<String> scannedClasses = new HashSet<String>();
        Set<String> seenSignatures = new HashSet<String>();

        if (prefix == null) {
            prefix = "";
        }

        addMethodsFromClass(prefix, kind, classFqn, methods, seenSignatures, scannedClasses, haveRedirected, false);

        return methods;
    }

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addMethodsFromClass(String prefix, QuerySupport.Kind kind, String classFqn,
        Set<IndexedMethod> methods, Set<String> seenSignatures, Set<String> scannedClasses,
        boolean haveRedirected, boolean inheriting) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = GroovyIndexer.FQN_NAME;

        Set<IndexResult> result = new HashSet<IndexResult>();

        search(searchField, classFqn, QuerySupport.Kind.EXACT, result);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        String extendsClass = null;

        String classIn = null;
        int fqnIndex = classFqn.lastIndexOf("."); // NOI18N

        if (fqnIndex != -1) {
            classIn = classFqn.substring(0, fqnIndex);
        }

        for (IndexResult map : result) {
            assert map != null;

            String[] signatures = map.getValues(GroovyIndexer.METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
                    if ((prefix.length() == 0) && !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Prevent duplicates when method is redefined
                    if (!seenSignatures.contains(signature)) {
                        if (signature.startsWith(prefix)) {
                            if (kind == QuerySupport.Kind.EXACT) {
                                // Ensure that the method is not longer than the prefix
                                if ((signature.length() > prefix.length()) &&
                                        (signature.charAt(prefix.length()) != '(') &&
                                        (signature.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == QuerySupport.Kind.PREFIX) ||
                                (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(signature);

                            IndexedMethod method = createMethod(signature, map, inheriting);
                            method.setSmart(!haveRedirected);
                            methods.add(method);
                        }
                    }
                }
            }

// XXX looks like copied from Ruby - getters and setters for properties are already indexed as methods
//            String[] attributes = map.getValues(GroovyIndexer.ATTRIBUTE_NAME);
//
//            if (attributes != null) {
//                for (String attribute : attributes) {
//                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
//                    if ((prefix.length() == 0) && !Character.isLowerCase(attribute.charAt(0))) {
//                        continue;
//                    }
//
//                    // Prevent duplicates when method is redefined
//                    if (!seenSignatures.contains(attribute)) {
//                        if (attribute.startsWith(prefix)) {
//                            if (kind == NameKind.EXACT_NAME) {
//                                // Ensure that the method is not longer than the prefix
//                                if ((attribute.length() > prefix.length()) &&
//                                        (attribute.charAt(prefix.length()) != '(') &&
//                                        (attribute.charAt(prefix.length()) != ';')) {
//                                    continue;
//                                }
//                            } else {
//                                // REGEXP, CAMELCASE filtering etc. not supported here
//                                assert (kind == NameKind.PREFIX) ||
//                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
//                            }
//
//                            seenSignatures.add(attribute);
//
//                            // TODO - create both getter and setter methods
//                            IndexedMethod method = createMethod(attribute, map, inheriting);
//                            method.setSmart(!haveRedirected);
//                            method.setMethodType(IndexedMethod.MethodType.ATTRIBUTE);
//                            methods.add(method);
//                        }
//                    }
//                }
//            }
        }

        if (extendsClass == null) {
            // XXX GroovyObject, GroovyScript
            addMethodsFromClass(prefix, kind, "java.lang.Object", methods, seenSignatures, scannedClasses,
                true, true);
        } else {
            // We're not sure we have a fully qualified path, so try some different candidates
            if (!addMethodsFromClass(prefix, kind, extendsClass, methods, seenSignatures,
                        scannedClasses, haveRedirected, true)) {
                // Search by classIn
                String fqn = classIn;

                while (fqn != null) {
                    if (addMethodsFromClass(prefix, kind, fqn + "." + extendsClass, methods,
                                seenSignatures, scannedClasses, haveRedirected, true)) {
                        break;
                    }

                    int f = fqn.lastIndexOf("."); // NOI18N

                    if (f == -1) {
                        break;
                    } else {
                        fqn = fqn.substring(0, f);
                    }
                }
            }
        }

        return foundIt;
    }

    private IndexedClass createClass(String fqn, String simpleName, IndexResult map) {

        // TODO - how do I determine -which- file to associate with the file?
        // Perhaps the one that defines initialize() ?

        if (simpleName == null) {
            simpleName = map.getValue(GroovyIndexer.CLASS_NAME);
        }

        String attrs = map.getValue(GroovyIndexer.CLASS_ATTRS);

        int flags = 0;
        if (attrs != null) {
            flags = IndexedElement.stringToFlag(attrs, 0);
        }

        IndexedClass c =
            IndexedClass.create(this, simpleName, fqn, map, attrs, flags);

        return c;
    }

    private IndexedMethod createMethod(String signature, IndexResult map, boolean inherited) {
        String clz = map.getValue(GroovyIndexer.CLASS_NAME);
        String module = map.getValue(GroovyIndexer.IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "." + clz; // NOI18N
        }

        String fqn = map.getValue(GroovyIndexer.FQN_NAME);

        int typeIndex = signature.indexOf(';');
        String methodSignature = signature;
        String type = "void";
        if (typeIndex != -1) {
            int endIndex = signature.indexOf(';', typeIndex + 1);
            if (endIndex == -1) {
                endIndex = signature.length();
            }
            type = signature.substring(typeIndex + 1, endIndex);
            methodSignature = signature.substring(0, typeIndex);
        }

        // Extract attributes
        int attributeIndex = signature.indexOf(';', typeIndex + 1);
        String attributes = null;
        int flags = 0;

        if (attributeIndex != -1) {
            flags = IndexedElement.stringToFlag(signature, attributeIndex+1);

            if (signature.length() > attributeIndex+1) {
                attributes = signature.substring(attributeIndex+1, signature.length());
            }

        }

        IndexedMethod m = IndexedMethod.create(this, methodSignature, type, fqn, clz, map, attributes, flags);

        m.setInherited(inherited);
        return m;
    }

    private IndexedField createField(String signature, IndexResult map, boolean inherited) {
        String clz = map.getValue(GroovyIndexer.CLASS_NAME);
        String module = map.getValue(GroovyIndexer.IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "." + clz; // NOI18N
        }

        String fqn = map.getValue(GroovyIndexer.FQN_NAME);

        int typeIndex = signature.indexOf(';');
        String name = signature;
        String type = "java.lang.Object";
        if (typeIndex != -1) {
            int endIndex = signature.indexOf(';', typeIndex + 1);
            if (endIndex == -1) {
                endIndex = signature.length();
            }
            type = signature.substring(typeIndex + 1, endIndex);
            name = signature.substring(0, typeIndex);
        }

        int attributeIndex = signature.indexOf(';', typeIndex + 1);
        String attributes = null;
        int flags = 0;

        if (attributeIndex != -1) {
            flags = IndexedElement.stringToFlag(signature, attributeIndex + 1);

            if (signature.length() > attributeIndex + 1) {
                attributes = signature.substring(attributeIndex + 1, signature.length());
            }

            //signature = signature.substring(0, attributeIndex);
        }

        IndexedField m =
            IndexedField.create(this, name, type, fqn, map, attributes, flags);
        m.setInherited(inherited);

        return m;
    }

    private boolean search(String key, String name, QuerySupport.Kind kind, Set<IndexResult> result) {
        try {
            //index.search(key, name, kind, ALL_SCOPE, result, null);
            result.addAll(querySupport.query(key, name, kind,  (String[]) null));
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
