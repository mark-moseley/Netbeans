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

package org.netbeans.modules.php.project.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.classpath.CommonPhpSourcePath;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * @author Tomas Mysik
 * @since 2.0
 */
public final class PhpSourcePath {
    public static final String  MIME_TYPE = "text/x-php5"; // NOI18N
    public static final String  DEBUG_SESSION =  "netbeans-xdebug"; // NOI18N

    private static final DefaultPhpSourcePath DEFAULT_PHP_SOURCE_PATH = new DefaultPhpSourcePath();

    /**
     * Possible types of a file.
     */
    public static enum FileType {
        /** Internal files (signature files). */
        INTERNAL,
        /** PHP include path. */
        INCLUDE,
        /** Project sources. */
        SOURCE,
        /** Unknown file type. */
        UNKNOWN,
    }

    private PhpSourcePath() {
    }

    /**
     * Get the file type for the given file object.
     * @param file the input file.
     * @return the file type for the given file object.
     * @see FileType
     */
    public static FileType getFileType(FileObject file) {
        Parameters.notNull("file", file);

        Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            return getPhpOptionsFromLookup(project).getFileType(file);
        }
        return DEFAULT_PHP_SOURCE_PATH.getFileType(file);
    }

    /**
     * Get all the possible path roots from PHP include path for the given file. If the file equals <code>null</code> then
     * just global PHP include path is returned.
     * @param file a file which could belong to a project or <code>null</code> for gettting global PHP include path.
     * @return all the possible path roots from PHP include path.
     */
    public static List<FileObject> getIncludePath(FileObject file) {
        if (file == null) {
            return DEFAULT_PHP_SOURCE_PATH.getIncludePath();
        }
        Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            return getPhpOptionsFromLookup(project).getIncludePath();
        }
        return DEFAULT_PHP_SOURCE_PATH.getIncludePath();
    }

    /**
     * Resolve absolute path for the given file name. The order is the given directory then PHP include path.
     * @param directory the directory to which the PHP <code>include()</code> or <code>require()</code> functions
     *                  could be resolved. Typically the directory containing the given script.
     * @param fileName a file name or a relative path delimited by '/'.
     * @return resolved file path or <code>null</code> if the given file is not found.
     */
    public static FileObject resolveFile(FileObject directory, String fileName) {
        Parameters.notNull("directory", directory);
        Parameters.notNull("fileName", fileName);
        if (!directory.isFolder()) {
            throw new IllegalArgumentException("valid directory needed");
        }

        Project project = FileOwnerQuery.getOwner(directory);
        if (project != null) {
            return getPhpOptionsFromLookup(project).resolveFile(directory, fileName);
        }
        return DEFAULT_PHP_SOURCE_PATH.resolveFile(directory, fileName);
    }

    private static org.netbeans.modules.php.project.classpath.PhpSourcePath getPhpOptionsFromLookup(Project project) {
        org.netbeans.modules.php.project.classpath.PhpSourcePath phpSourcePath =
                project.getLookup().lookup(org.netbeans.modules.php.project.classpath.PhpSourcePath.class);
        assert phpSourcePath != null : "Not PHP project (interface PhpSourcePath not found in lookup)! [" + project + "]";
        return phpSourcePath;
    }

    // PhpSourcePath implementation for file which does not belong to any project
    private static class DefaultPhpSourcePath implements org.netbeans.modules.php.project.classpath.PhpSourcePath {

        public FileType getFileType(FileObject file) {
            FileObject path = CommonPhpSourcePath.getInternalPath();
            if (path.equals(file) || FileUtil.isParentOf(path, file)) {
                return FileType.INTERNAL;
            }
            for (FileObject dir : getPlatformPath()) {
                if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                    return FileType.INCLUDE;
                }
            }
            return FileType.UNKNOWN;
        }

        public List<FileObject> getIncludePath() {
            return new ArrayList<FileObject>(getPlatformPath());
        }

        public FileObject resolveFile(FileObject directory, String fileName) {
            FileObject resolved = directory.getFileObject(fileName);
            if (resolved != null) {
                return resolved;
            }
            for (FileObject dir : getPlatformPath()) {
                resolved = dir.getFileObject(fileName);
                if (resolved != null) {
                    return resolved;
                }
            }
            return null;
        }

        // XXX cache?
        private List<FileObject> getPlatformPath() {
            String phpGlobalIncludePath = PhpOptions.getInstance().getPhpGlobalIncludePath();
            assert phpGlobalIncludePath != null;
            String[] paths = PropertyUtils.tokenizePath(phpGlobalIncludePath);
            List<FileObject> dirs = new ArrayList<FileObject>(paths.length);
            for (String path : paths) {
                FileObject resolvedFile = FileUtil.toFileObject(new File(path));
                if (resolvedFile != null) { // XXX check isValid() as well?
                    dirs.add(resolvedFile);
                }
            }
            return dirs;
        }
    }
}
