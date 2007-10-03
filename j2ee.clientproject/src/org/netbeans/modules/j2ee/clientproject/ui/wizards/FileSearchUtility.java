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

package org.netbeans.modules.j2ee.clientproject.ui.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.clientproject.AppClientProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;

final class FileSearchUtility {
    
    /** Creates a new instance of FileSearchUtility. */
    private FileSearchUtility() {
    }
        
   /** Recursively enumerate all children of this folder to some specified depth.
    *  All direct children are listed; then children of direct subfolders; and so on.
    *
    * @param root the starting directory
    * @param depth the search limit
    * @param onlyWritables only recurse into writable directories
    * @return enumeration of type <code>FileObject</code>
    */
    static Enumeration<FileObject> getChildrenToDepth(final FileObject root, final int depth, final boolean onlyWritables) {
        class WithChildren implements Enumerations.Processor<FileObject, FileObject> {
            private final int rootDepth;
            public WithChildren(final int rootDepth) {
                this.rootDepth = rootDepth;
            }
            
            public FileObject process(final FileObject obj, final Collection<FileObject> toAdd) {
                FileObject fo = obj;
                if (!onlyWritables || (onlyWritables && fo.canWrite())) {
                    if (fo.isFolder() && (getDepth(fo) - rootDepth) < depth) {
                        toAdd.addAll(Arrays.asList(fo.getChildren()));
                    }
                }
                return fo;
            }
        }

        return Enumerations.queue(
            Enumerations.array(root.getChildren()),
            new WithChildren(getDepth(root))
        );
    }

    static FileObject[] guessJavaRoots(final FileObject dir) {
        List<FileObject> foundRoots = new ArrayList<FileObject>();
        if (null == dir) {
            return null;
        }
        Enumeration<FileObject> ch = FileSearchUtility.getChildrenToDepth(dir, 10, true); // .getChildren(true);
        try {
            // digging through 10 levels exhaustively is WAY TOO EXPENSIVE
            while (ch.hasMoreElements () && foundRoots.isEmpty()) {
                FileObject f = ch.nextElement ();
                if (f.getExt().equals("java") && !f.isFolder()) { //NOI18N
                    String pckg = guessPackageName(f);
                    String pkgPath = f.getParent().getPath(); 
                    if (pckg != null && pkgPath.endsWith(pckg.replace('.', '/'))) {
                        String rootName = pkgPath.substring(0, pkgPath.length() - pckg.length());
                        FileObject fr = f.getFileSystem().findResource(rootName);
                        if (!foundRoots.contains(fr)) {
                            foundRoots.add(fr);
                        }
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            Logger.getLogger("global").log(Level.INFO, null, fsie);
        }
        if (foundRoots.size() == 0) {
            return null;
        } else {
            FileObject[] resultArr = new FileObject[foundRoots.size()];
            for (int i = 0; i < foundRoots.size(); i++) {
                resultArr[i] = foundRoots.get(i);
            }
            return resultArr;
        }
    }
    
    static   FileObject guessConfigFilesPath(final FileObject dir) {
        if (null == dir) {
            return null;
        }
        Enumeration<FileObject> ch = FileSearchUtility.getChildrenToDepth(dir, 3, true); //getChildren(true);
        try {
            while (ch.hasMoreElements()) {
                FileObject f = ch.nextElement();
                if (f.getNameExt().equals(AppClientProvider.FILE_DD)) {
                    String rootName = f.getParent().getPath();
                    return f.getFileSystem().findResource(rootName);
                }
            }
        } catch (FileStateInvalidException fsie) {
            Logger.getLogger("global").log(Level.INFO, null, fsie);
        }
        return null;
    }


    static  File[] guessJavaRootsAsFiles(final FileObject dir) {
        FileObject[] rootsFOs = guessJavaRoots(dir);
        if (rootsFOs == null) {
            return new File[0];
        }
        File[] resultArr = new File[rootsFOs.length];
        for (int i = 0; i < resultArr.length; i++) {
            resultArr[i] = FileUtil.toFile(rootsFOs[i]);
        }
        return resultArr;
    }

    private static String guessPackageName(final FileObject f) {
        Reader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(f.getInputStream(), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer();
            final char[] buffer = new char[4096];
            int len;

            for (;;) {
                len = r.read(buffer);
                if (len == -1) { break; }
                sb.append(buffer, 0, len);
            }
            int idx = sb.indexOf("package"); // NOI18N
            if (idx >= 0) {
                int idx2 = sb.indexOf(";", idx); // NOI18N
                if (idx2 >= 0) {
                    return sb.substring(idx + "package".length(), idx2).trim(); // NOI18N
                }
            }
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException ioe) {
                // ignore this
            }
        }
        // AB: fix for #56160: assume the class is in the default package
        return "";
    }

    static FileObject guessLibrariesFolder(final FileObject dir) {
        if (dir != null) {
            FileObject lib = dir.getFileObject("lib"); //NOI18N
            if (lib != null) {
                return lib;
            }
        }
        Enumeration<FileObject> ch = FileSearchUtility.getChildrenToDepth(dir, 3, true);
        while (ch.hasMoreElements()) {
            FileObject f = ch.nextElement();
            if (f.getExt().equals("jar")) { //NOI18N
                return f.getParent();
            }
        }
        return null;
    }

    private static int getDepth(final FileObject fo) {
        String path = FileUtil.toFile(fo).getAbsolutePath();
        StringTokenizer toker = new StringTokenizer(path, File.separator);
        return toker.countTokens();
    }
}
