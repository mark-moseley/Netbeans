/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.java.queries;

import java.net.URL;
import org.openide.filesystems.FileObject;

/**
 * Query to find Java package root of unit tests for Java package root of 
 * sources and vice versa.
 *
 * <p>A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the binary file and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.</p>
 *
 * <p>This interface assumes following mapping pattern between source
 * files and unit tests: <code>*.java -> *Test.java</code>. This mapping
 * is used for example for unit test generation and for searching test for
 * source. Usage of any other pattern will break this functionality.</p>
 *
 * @see <a href="@PROJECTS/PROJECTAPI/org/netbeans/api/project/Project.html#getLookup"><code>Project.getLookup()</code></a>
 * @see org.netbeans.api.java.queries.UnitTestForSourceQuery
 * @deprecated Use {@link org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation} class.
 * @author David Konecny
 * @since org.netbeans.api.java/1 1.4
 */
public interface UnitTestForSourceQueryImplementation {
    
    /**
     * Returns the test root for a given source root.
     *
     * @param source a Java package root with sources
     * @return a corresponding Java package root with unit tests. The
     *     returned URL need not point to an existing folder. It can be null
     *     when no mapping from source to unit test is known.
     */
    URL findUnitTest(FileObject source);
    
    /**
     * Returns the source root for a given test root.
     *
     * @param unitTest a Java package root with unit tests
     * @return a corresponding Java package root with sources. It can be null
     *     when no mapping from unit test to source is known.
     */
    URL findSource(FileObject unitTest);
    
}
