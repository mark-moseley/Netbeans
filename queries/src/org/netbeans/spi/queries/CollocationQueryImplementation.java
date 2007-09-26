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

package org.netbeans.spi.queries;

import java.io.File;

/**
 * A query which should typically be provided by a VCS to give information
 * about whether some files can be considered part of one logical directory tree.
 * <p>
 * This should be treated as a heuristic, useful when deciding whether to use
 * absolute or relative links between path locations.
 * </p>
 * <p>
 * The file names might refer to nonexistent files. A provider may or may not
 * be able to say anything useful about them in this case.
 * </p>
 * <p>
 * File names passed to this query will already have been normalized according to
 * the semantics of {@link org.openide.filesystems.FileUtil#normalizeFile}.
 * </p>
 * <p>
 * Threading note: implementors should avoid acquiring locks that might be held
 * by other threads. Generally treat this interface similarly to SPIs in
 * {@link org.openide.filesystems} with respect to threading semantics.
 * </p>
 * @see org.netbeans.api.queries.CollocationQuery
 * @author Jesse Glick
 */
public interface CollocationQueryImplementation {
    
    /**
     * Check whether two files are logically part of one directory tree.
     * For example, if both files are stored in CVS, with the same server
     * (<code>CVSROOT</code>) they might be considered collocated.
     * If they are to be collocated their absolute paths must share a
     * prefix directory, i.e. they must be located in the same filesystem root.
     * If nothing is known about them, return false.
     * @param file1 one file
     * @param file2 another file
     * @return true if they are probably part of one logical tree
     */
    boolean areCollocated(File file1, File file2);
    
    /**
     * Find a root of a logical tree containing this file, if any.
     * The path of the root (if there is one) must be a prefix of the path of the file.
     * @param file a file on disk (must be an absolute URI)
     * @return an ancestor directory which is the root of a logical tree,
     *         if any (else null) (must be an absolute URI)
     */
    File findRoot(File file);
    
}
