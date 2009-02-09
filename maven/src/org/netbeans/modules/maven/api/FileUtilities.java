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

package org.netbeans.modules.maven.api;

import java.io.File;
import java.net.URI;
import java.util.Stack;
import java.util.regex.Pattern;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Various File/FileObject related utilities.
 * @author  Milos Kleint
 */
public final class FileUtilities {
    
    /** Creates a new instance of FileUtilities */
    private FileUtilities() {
    }
    
    public static FileObject convertURItoFileObject(URI uri) {
        if (uri == null) {
            return null;
        }
        File fil = new File(uri);
        return FileUtil.toFileObject(fil);
    }
    
    public static FileObject convertStringToFileObject(String str) {
        if (str != null) {
            File fil = new File(str);
            fil = FileUtil.normalizeFile(fil);
            return FileUtil.toFileObject(fil);
        }
        return null;
    }
    
    public static File convertStringToFile(String str) {
        if (str != null) {
            File fil = new File(str);
            return FileUtil.normalizeFile(fil);
        }
        return null;
    }
    

    public static URI convertStringToUri(String str) {
        if (str != null) {
            File fil = new File(str);
            fil = FileUtil.normalizeFile(fil);
            return fil.toURI();
        }
        return null;
    }

    /**
     * take any (even unresolved) Maven Arfifact instance and construct a local
     * repository based File instance for it. The file does not have to exist though.
     * @param artifact
     * @return
     */
    public static File convertArtifactToLocalRepositoryFile(Artifact artifact) {
        String path = EmbedderFactory.getProjectEmbedder().getLocalRepository().pathOf(artifact);
        File base = convertStringToFile(EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir());
        return resolveFilePath(base, path);
    }

    private static final Pattern RELATIVE_SLASH_SEPARATED_PATH = 
            Pattern.compile("[^:/\\\\.][^:/\\\\]*(/[^:/\\\\.][^:/\\\\]*)*"); // NOI18N
     
    /**
     * copied from netbeans.org's ant/project sources. will find out if path is relative or absolute
     */
    public static File resolveFilePath(File basedir, String filename) {
        if (basedir == null) {
            throw new NullPointerException("null basedir passed to resolveFile"); // NOI18N
        }
        if (filename == null) {
            throw new NullPointerException("null filename passed to resolveFile"); // NOI18N
        }
        if (!basedir.isAbsolute()) {
            throw new IllegalArgumentException("nonabsolute basedir passed to resolveFile: " + basedir); // NOI18N
        }
        File f;
        if (RELATIVE_SLASH_SEPARATED_PATH.matcher(filename).matches()) {
            // Shortcut - simple relative path. Potentially faster.
            f = new File(basedir, filename.replace('/', File.separatorChar));
        } else {
            // All other cases.
            String machinePath = filename.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            f = new File(machinePath);
            if (!f.isAbsolute()) {
                f = new File(basedir, machinePath);
            }
            assert f.isAbsolute();
        }
        return FileUtil.normalizeFile(f);    
    }
    
   public static URI getDirURI(File root, String path) {
       String pth = path.trim();
       pth = pth.replaceFirst("^\\./", ""); //NOI18N
       pth = pth.replaceFirst("^\\.\\\\", ""); //NOI18N
       File src = FileUtilities.resolveFilePath(root, pth);
       return FileUtil.normalizeFile(src).toURI();
   }
    
   public static URI getDirURI(FileObject root, String path) {
       return getDirURI(FileUtil.toFile(root), path);
   }

//copied from o.o.f.FileUtil
   /**
    * get relative path between file and it's child. if not child, return null.
    * @param dir
    * @param file
    * @return
    */
    public static String getRelativePath(final File dir, final File file) {
        Stack<String> stack = new Stack<String>();
        File tempFile = file;
        while(tempFile != null && !tempFile.equals(dir)) {
            stack.push (tempFile.getName());
            tempFile = tempFile.getParentFile();
        }
        if (tempFile == null) {
            return null;
        }
        StringBuilder retval = new StringBuilder();
        while (!stack.isEmpty()) {
            retval.append(stack.pop());
            if (!stack.isEmpty()) {
                retval.append('/');//NOI18N
            }
        }                        
        return retval.toString();
    }
    
    /**
     * force refreshes of Filesystem to make the conversion to FileObject work.
     * 
     * @param file
     * @return
     * @deprecated Use FileUtil.refreshFor() + FileUtil.toFileObject()
     */
    public @Deprecated static FileObject toFileObject(File fl) {
        FileUtil.refreshFor(fl);
        return FileUtil.toFileObject(fl);
    }

    /**
     *
     * Produce a machine-independent relativized version of a filename from a basedir.
     * Unlike {@link URI#relativize} this will produce "../" sequences as needed.
     * @param basedir a directory to resolve relative to (need not exist on disk)
     * @param file a file or directory to find a relative path for
     * @return a relativized path (slash-separated), or null if it is not possible (e.g. different DOS drives);
     *         just <samp>.</samp> in case the paths are the same
     * @throws IllegalArgumentException if the basedir is known to be a file and not a directory
     *
     * copied from project.ant's PropertyUtils
     */
    public static String relativizeFile(File basedir, File file) {
        if (basedir.isFile()) {
            throw new IllegalArgumentException("Cannot relative w.r.t. a data file " + basedir); // NOI18N
        }
        if (basedir.equals(file)) {
            return "."; // NOI18N
        }
        StringBuffer b = new StringBuffer();
        File base = basedir;
        String filepath = file.getAbsolutePath();
        while (!filepath.startsWith(slashify(base.getAbsolutePath()))) {
            base = base.getParentFile();
            if (base == null) {
                return null;
            }
            if (base.equals(file)) {
                // #61687: file is a parent of basedir
                b.append(".."); // NOI18N
                return b.toString();
            }
            b.append("../"); // NOI18N
        }
        URI u = base.toURI().relativize(file.toURI());
        assert !u.isAbsolute() : u + " from " + basedir + " and " + file + " with common root " + base;
        b.append(u.getPath());
        if (b.charAt(b.length() - 1) == '/') {
            // file is an existing directory and file.toURI ends in /
            // we do not want the trailing slash
            b.setLength(b.length() - 1);
        }
        return b.toString();
    }

    private static String slashify(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separatorChar;
        }
    }

   
}
