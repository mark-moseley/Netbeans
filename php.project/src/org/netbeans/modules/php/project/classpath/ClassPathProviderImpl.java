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
package org.netbeans.modules.php.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathFactory;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathProvider;
import org.netbeans.modules.php.project.PhpSources;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Defines the various (BOOT and SOURCE) class paths for a PHP project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {

    /**
     * Type of file classpath is required for.
     */
    public static enum FileType {
        INTERNAL, // nb internal files (signature files)
        PLATFORM, // php include path
        SOURCE, // project sources
        UNKNOWN,
    }

    /**
     * Constants for different cached classpaths.
     */
    private static enum ClassPathCache {
        PLATFORM,
        SOURCE,
    }

    // GuardedBy(this)
    private static FileObject internalFolder = null;

    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final PhpSources sourceRoots;

    // GuardedBy(this)
    private final Map<String, List<FileObject>> dirCache = new HashMap<String, List<FileObject>>();
    // GuardedBy(this)
    private final Map<ClassPathCache, ClassPath> cache = new EnumMap<ClassPathCache, ClassPath>(ClassPathCache.class);

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, PhpSources sources) {
        this.helper = helper;
        projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sources;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    private synchronized List<FileObject> getDirs(String propname) {
        List<FileObject> dirs = dirCache.get(propname);
        if (!checkDirs(dirs)) {
            String prop = evaluator.getProperty(propname);
            if (prop == null) {
                return Collections.<FileObject>emptyList();
            }
            String[] paths = PropertyUtils.tokenizePath(prop);
            dirs = new ArrayList<FileObject>(paths.length);
            for (String path : paths) {
                FileObject resolvedFile = helper.resolveFileObject(path);
                if (resolvedFile != null) {
                    dirs.add(resolvedFile);
                }
            }
            dirCache.put(propname, dirs);
        }
        return dirs;
    }

    private boolean checkDirs(List<FileObject> dirs) {
        if (dirs == null) {
            return false;
        }
        for (FileObject fo : dirs) {
            if (!fo.isValid()) {
                return false;
            }
        }
        return true;
    }

    private synchronized FileObject getInternalPath() {
        if (internalFolder == null) {
            // XXX workaround because gsf uses toFile() and this causes NPE for SFS
            // XXX filesystem listener should be used
            FileObject sfsFolder = Repository.getDefault().getDefaultFileSystem().findResource("PHP/RuntimeLibraries"); // NOI18N
            for (FileObject fo : sfsFolder.getChildren()) {
                if (FileUtil.toFile(fo) != null) {
                    continue;
                }
                InputStream is = null;
                OutputStream os = null;
                ByteArrayOutputStream bos = null;
                try {
                    is = fo.getInputStream();
                    os = fo.getOutputStream();
                    bos = new ByteArrayOutputStream();
                    FileUtil.copy(is, bos);
                    os.write(bos.toByteArray());
                } catch (IOException exc) {
                    Exceptions.printStackTrace(exc);
                } finally {
                    closeStreams(is, os, bos);
                }
            }
            File file = FileUtil.toFile(sfsFolder);
            assert file != null : "Folder PHP/RuntimeLibraries cannot be resolved as a java.io.File";
            internalFolder = FileUtil.toFileObject(file);
        }
        return internalFolder;
    }

    private void closeStreams(Closeable... streams) {
        for (Closeable stream : streams) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private List<FileObject> getPlatformPath() {
        return getDirs(PhpProjectProperties.INCLUDE_PATH);
    }

    private FileObject getSrcPath() {
        List<FileObject> dirs = getDirs(PhpProjectProperties.SRC_DIR);
        if (dirs.size() == 0) {
            // non-existing directory??
            return null;
        }
        assert dirs.size() == 1; // one source directory is allowed
        return dirs.get(0);
    }

    /**
     * Get the file type for the given file object.
     * @param file the input file.
     * @return the file type for the given file object.
     * @see FileType
     */
    public FileType getFileType(FileObject file) {
        Parameters.notNull("file", file);

        FileObject path = getInternalPath();
        if (path.equals(file) || FileUtil.isParentOf(path, file)) {
            return FileType.INTERNAL;
        }
        for (FileObject dir : getPlatformPath()) {
            if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                return FileType.PLATFORM;
            }
        }
        path = getSrcPath();
        if (path != null
                && (path.equals(file) || FileUtil.isParentOf(path, file))) {
            return FileType.SOURCE;
        }
        return FileType.UNKNOWN;
    }

    /**
     * Get all the possible path roots from PHP include path.
     * @return all the possible path roots from PHP include path.
     */
    public List<FileObject> getIncludePath() {
        return new ArrayList<FileObject>(getPlatformPath());
    }


    /**
     * Resolve absolute path for the given file name. The order is the given directory then PHP include path.
     * @param directory the directory to which the PHP <code>include()</code> or <code>require()</code> functions
     *                  could be resolved. Typically the directory containing the given script.
     * @param fileName a file name or a relative path delimited by '/'.
     * @return resolved file path or <code>null</code> if the given file is not found.
     */
    public FileObject resolveFile(FileObject directory, String fileName) {
        Parameters.notNull("directory", directory);
        Parameters.notNull("fileName", fileName);

        if (!directory.isFolder()) {
            throw new IllegalArgumentException("valid directory needed");
        }

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

    private ClassPath getSourcePath(FileObject file) {
        return getSourcePath(getFileType(file));
    }

    private synchronized ClassPath getSourcePath(FileType type) {
        ClassPath cp = null;
        switch (type) {
            case SOURCE:
                cp = cache.get(ClassPathCache.SOURCE);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(sourceRoots, helper, evaluator));
                    cache.put(ClassPathCache.SOURCE, cp);
                }
                break;
            default:
                // XXX any exception?
                break;
        }
        return cp;
    }

    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache.get(ClassPathCache.PLATFORM);
        if (cp == null) {
            FileObject internalPath = getInternalPath();
            List<FileObject> platformPath = getPlatformPath();
            List<FileObject> cpItems = new ArrayList<FileObject>(platformPath.size() + 1);
            cpItems.add(internalPath);
            cpItems.addAll(platformPath);

            cp = org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport.createClassPath(
                    cpItems.toArray(new FileObject[cpItems.size()]));
            cache.put(ClassPathCache.PLATFORM, cp);
        }
        return cp;
    }

    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcePath(file);
        } else if (type.equals(ClassPath.COMPILE)) {
            // ???
            return getBootClassPath();
        }
        assert false : "Unknown classpath type requested: " + type;
        return null;
    }

    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[] {getBootClassPath()};
        } else if (ClassPath.SOURCE.equals(type)) {
            return new ClassPath[] {getSourcePath(FileType.SOURCE)};
        }
        assert false : "Unknown classpath type requested: " + type;
        return null;
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
}
