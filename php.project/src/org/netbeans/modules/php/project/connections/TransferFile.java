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

package org.netbeans.modules.php.project.connections;

import java.io.File;
import org.apache.commons.net.ftp.FTPFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * File to be transfered to/from remote server. It is able to resolve different relative paths
 * against some base directory. This is useful e.g. while uploading:
 * <pre>
 * /home/test/Project1/web/test.php => /pub/Project1/web/test.php,
 * </pre>
 * then for base path "/home/test/Project1" the relative path would be "web/test.php".
 * <p>
 * Instances can be neither directories nor files; this applies for {@link #fromPath(java.lang.String)}.
 * <p>
 * Path separator is always {@value #SEPARATOR}, for all platforms.
 * @author Tomas Mysik
 */
public final class TransferFile {
    public static final String SEPARATOR = "/"; // NOI18N
    public static final String CWD = "."; // NOI18N

    private final String name;
    private final String relativePath;
    private final String parentRelativePath;
    private final long size; // in bytes
    private final boolean directory;
    private final boolean file;

    private TransferFile(String name, String relativePath, String parentRelativePath, long size, boolean directory, boolean file) {
        assert (directory && !file) || (!directory && file) : "File has to be exactly directory or file";
        assert size >= 0L : "Size cannot be smaller than 0";
        if (directory && size != 0L) {
            throw new IllegalArgumentException("Size of a directory has to be 0 bytes");
        }

        this.name = name;
        this.relativePath = relativePath;
        this.parentRelativePath = parentRelativePath;
        this.directory = directory;
        this.file = file;
        this.size = size;
    }

    /**
     * Implementation for {@link File}.
     */
    public static TransferFile fromFile(File file, String baseDirectory) {
        assert file != null;
        assert baseDirectory != null;

        file = FileUtil.normalizeFile(file);

        assert file.getAbsolutePath().startsWith(baseDirectory) : "File must be underneath base directory [" + file.getAbsolutePath() + " => " + baseDirectory + "]";

        String name = file.getName();
        String relativePath = getPlatformIndependentPath(getRelativePath(file.getAbsolutePath(), baseDirectory));
        String parentRelativePath = getPlatformIndependentPath(getParentRelativePath(file.getParentFile().getAbsolutePath(), baseDirectory));
        boolean directory = file.isDirectory();
        boolean f = file.isFile();
        long size = directory ? 0L : file.length();

        return new TransferFile(name, relativePath, parentRelativePath, size, directory, f);
    }

    /**
     * Implementation for {@link FileObject}.
     */
    public static TransferFile fromFileObject(FileObject fo, String baseDirectory) {
        assert fo != null;

        return fromFile(FileUtil.toFile(fo), baseDirectory);
    }

    /**
     * Implementation for {@link FTPFile}.
     */
    public static TransferFile fromFtpFile(FTPFile ftpFile, String baseDirectory, String parentDirectory) {
        assert ftpFile != null;
        assert baseDirectory.startsWith(SEPARATOR) : "Base directory must start with '" + SEPARATOR + "' [" + baseDirectory + "]";
        assert parentDirectory.startsWith(SEPARATOR) : "Parent directory must start with '" + SEPARATOR + "' [" + parentDirectory + "]";
        assert !baseDirectory.endsWith(SEPARATOR) && !parentDirectory.endsWith(SEPARATOR) : "Both base and parent directory cannot end with '" + SEPARATOR + "' [" + baseDirectory + ", " + parentDirectory + "]";
        assert parentDirectory.startsWith(baseDirectory) : "Parent directory must be underneath base directory [" + parentDirectory + " => " + baseDirectory + "]";

        String name = ftpFile.getName();
        String absolutePath = parentDirectory + SEPARATOR + name; // NOI18N
        String relativePath = getRelativePath(absolutePath, baseDirectory);
        String parentRelativePath = getParentRelativePath(parentDirectory, baseDirectory);
        boolean directory = ftpFile.isDirectory();
        boolean file = ftpFile.isFile();
        long size = directory ? 0L : ftpFile.getSize();

        return new TransferFile(name, relativePath, parentRelativePath, size, directory, file);
    }

    /**
     * Dummy implementation for file path.
     */
    public static TransferFile fromPath(String path) {
        assert path != null;

        return new TransferFile(path, path, path, 0L, false, false);
    }

    private static String getRelativePath(String absolutePath, String baseDirectory) {
        if (absolutePath.equals(baseDirectory)) {
            return CWD;
        }
        // +1 => remove '/' from the beginning of the relative path
        return absolutePath.substring(baseDirectory.length() + SEPARATOR.length());
    }

    private static String getParentRelativePath(String parentPath, String baseDirectory) {
        if (parentPath.length() < baseDirectory.length()) {
            // out of scope
            return null;
        } else if (parentPath.equals(baseDirectory)) {
            return CWD;
        }
        // +1 => remove '/' from the beginning of the relative path
        return parentPath.substring(baseDirectory.length() + SEPARATOR.length());
    }

    /**
     * Helper method to convert path to platform independent. Separator is {@value #SEPARATOR}.
     * @param path path to convert, can be <code>null</code>.
     * @return platform independent path or <code>null</code>.
     * @see #SEPARATOR
     */
    private static String getPlatformIndependentPath(String path) {
        if (path == null) {
            return null;
        }
        if (File.separator.equals(SEPARATOR)) {
            return path;
        }
        return path.replace(File.separator, SEPARATOR);
    }

    /**
     * Helper method to convert path to platform dependent. Separator is {@link File#separator}.
     * @param path path to convert, can be <code>null</code>.
     * @return platform dependent path or <code>null</code>.
     */
    private static String getPlatformDependentPath(String path) {
        if (path == null) {
            return null;
        }
        if (File.separator.equals(SEPARATOR)) {
            return path;
        }
        return path.replace(SEPARATOR, File.separator);
    }

    public String getName() {
        return name;
    }

    /**
     * Get platform independent relative path or {@value #CWD} if absolute path equals relative path.
     * @see #CWD
     */
    public String getRelativePath() {
        return getRelativePath(false);
    }

    /**
     * Get relative path or {@value #CWD} if absolute path equals relative path.
     * @param platformDependent <code>true</code> for platform dependent relative path
     * @see #CWD
     */
    public String getRelativePath(boolean platformDependent) {
        if (platformDependent) {
            return getPlatformDependentPath(relativePath);
        }
        return relativePath;
    }

    /**
     * Get platform independent relative parent path, {@value #CWD} if parent path
     * equals base directory and <code>null</code> if parent path is not underneath
     * base directory (normally, it would start with "..").
     * @see #CWD
     */
    public String getParentRelativePath() {
        return getParentRelativePath(false);
    }

    /**
     * Get relative parent path, {@value #CWD} if parent path equals base directory
     * and <code>null</code> if parent path is not underneath base directory
     * (normally, it would start with "..").
     * @param platformDependent <code>true</code> for platform dependent relative path
     * @see #CWD
     */
    public String getParentRelativePath(boolean platformDependent) {
        if (platformDependent) {
            return getPlatformDependentPath(parentRelativePath);
        }
        return parentRelativePath;
    }

    /**
     * Get the size of the file in bytes. For directory it is always 0 (zero).
     * @return get the size of the file in bytes.
     */
    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isFile() {
        return file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TransferFile other = (TransferFile) obj;
        return relativePath.equals(other.relativePath);
    }

    @Override
    public int hashCode() {
        return relativePath.hashCode();
    }

    @Override
    public String toString() {
        return relativePath;
    }
}
