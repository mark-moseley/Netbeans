/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.classpath;

import java.util.List;
import org.netbeans.modules.php.project.api.PhpSourcePath.FileType;
import org.openide.filesystems.FileObject;

/**
 * @author Tomas Mysik
 */
public interface PhpSourcePath {

    /**
     * Get the file type for the given file object.
     * @param file the input file.
     * @return the file type for the given file object.
     * @see FileType
     */
    FileType getFileType(FileObject file);

    /**
     * Get all the possible path roots from PHP include path.
     * @return all the possible path roots from PHP include path.
     */
    List<FileObject> getIncludePath();

    /**
     * Resolve absolute path for the given file name. The order is the given directory then PHP include path.
     * @param directory the directory to which the PHP <code>include()</code> or <code>require()</code> functions
     *                  could be resolved. Typically the directory containing the given script.
     * @param fileName a file name or a relative path delimited by '/'.
     * @return resolved file path or <code>null</code> if the given file is not found.
     */
    FileObject resolveFile(FileObject directory, String fileName);
}
