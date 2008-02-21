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

package org.netbeans.sfpi.gsfpath.queries;

import java.net.URL;
import org.openide.filesystems.FileObject;

/**
 * Query to find Java package root of unit tests for Java package root of
 * sources and vice versa.
 *
 * <p>A default implementation is registered by the
 * <code>org.netbeans.modules.gsfpath.project</code> module which looks up the
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
 * @see org.netbeans.fpi.gsfpath.queries.UnitTestForSourceQuery
 * @deprecated Use {@link org.netbeans.sfpi.gsfpath.queries.MultipleRootsUnitTestForSourceQueryImplementation} instead.
 * @author David Konecny
 * @since org.netbeans.fpi.gsfpath/1 1.4
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
