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

package org.netbeans.api.gsfpath.queries;

import java.net.URL;
import org.netbeans.spi.gsfpath.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 * Query to find Java package root of unit tests for Java package root of
 * sources and vice versa.
 *
 * @see org.netbeans.spi.gsfpath.queries.MultipleRootsUnitTestForSourceQueryImplementation
 * @author David Konecny, Tomas Zezula
 * @since org.netbeans.api.gsfpath/1 1.4
 */
public class UnitTestForSourceQuery {

    @SuppressWarnings ("deprecation")   // NOI18N
    private static final Lookup.Result<? extends org.netbeans.spi.gsfpath.queries.UnitTestForSourceQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(org.netbeans.spi.gsfpath.queries.UnitTestForSourceQueryImplementation.class);
    private static final Lookup.Result<? extends MultipleRootsUnitTestForSourceQueryImplementation> mrImplementations = 
        Lookup.getDefault().lookupResult(MultipleRootsUnitTestForSourceQueryImplementation.class);

    private UnitTestForSourceQuery() {
    }

    /**
     * Returns the test root for a given source root.
     *
     * @param source Java package root with sources
     * @return corresponding Java package root with unit tests. The
     *     returned URL does not have to point to existing file. It can be null
     *     when the mapping from source to unit test is not known.
     * @deprecated Use {@link #findUnitTests} instead.
     */
    public static URL findUnitTest(FileObject source) {
        URL[] result = findUnitTests (source);
        return result.length == 0 ? null : result[0];
    }


    /**
     * Returns the test roots for a given source root.
     *
     * @param source Java package root with sources
     * @return corresponding Java package roots with unit tests. The
     *     returned URLs do not have to point to existing files. It can be an empty
     *     array (but not null) when the mapping from source to unit tests is not known.
     * @since org.netbeans.api.gsfpath/1 1.7
     */
    @SuppressWarnings ("deprecation")   // NOI18N
    public static URL[] findUnitTests(FileObject source) {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source cannot be null"); // NOI18N
        }
        for (MultipleRootsUnitTestForSourceQueryImplementation query : mrImplementations.allInstances()) {            
            URL[] urls = query.findUnitTests(source);
            if (urls != null) {
                return urls;
            }
        }
        for (org.netbeans.spi.gsfpath.queries.UnitTestForSourceQueryImplementation query : implementations.allInstances()) {
            URL u = query.findUnitTest(source);
            if (u != null) {
                return new URL[] {u};
            }
        }
        return new URL[0];
    }

    /**
     * Returns the source root for a given test root.
     *
     * @param unitTest Java package root with unit tests
     * @return corresponding Java package root with sources. It can be null
     *     when the mapping from unit test to source is not known.
     * @deprecated Use {@link #findSources} instead.
     */
    public static URL findSource(FileObject unitTest) {
        URL[] result =  findSources (unitTest);
        return result.length == 0 ? null : result[0];
    }

    /**
     * Returns the source roots for a given test root.
     *
     * @param unitTest Java package root with unit tests
     * @return corresponding Java package roots with sources. It can be an empty array (not null)
     *     when the mapping from unit test to sources is not known.
     * @since org.netbeans.api.gsfpath/1 1.7
     */
    @SuppressWarnings ("deprecation")   // NOI18N
    public static URL[] findSources (FileObject unitTest) {
        if (unitTest == null) {
            throw new IllegalArgumentException("Parameter unitTest cannot be null"); // NOI18N
        }
        for (MultipleRootsUnitTestForSourceQueryImplementation query : mrImplementations.allInstances()) {
            URL[] urls = query.findSources(unitTest);
            if (urls != null) {
                return urls;
            }
        }
        for (org.netbeans.spi.gsfpath.queries.UnitTestForSourceQueryImplementation query : implementations.allInstances()) {            
            URL u = query.findSource(unitTest);
            if (u != null) {
                return new URL[] {u};
            }
        }
        return new URL[0];
    }

}
