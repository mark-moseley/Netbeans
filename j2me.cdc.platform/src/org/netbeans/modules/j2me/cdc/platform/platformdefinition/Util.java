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

package org.netbeans.modules.j2me.cdc.platform.platformdefinition;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class Util {

    private Util () {
    }

    public static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List<PathResourceImplementation> list = new ArrayList<PathResourceImplementation>();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));
            URL url = getRootURL (f);
            if (url!=null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }

    // XXX this method could probably be removed... use standard FileUtil stuff
    static URL getRootURL  (File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(f.toURI().toURL());
            }
            else {
                url = f.toURI().toURL();
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL (surl+"/");
                }
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);            
        }
        return url;
    }


    /**
     * Returns normalized name from display name.
     * The normalized name should be used in the Ant properties and external files.
     * @param displayName
     * @return String
     */
    public static String normalizeName (String displayName) {
        StringBuffer normalizedName = new StringBuffer ();
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c =='-' || c =='.') {
                normalizedName.append(c);
            }
            else {
                normalizedName.append('_');
            }
        }
        return normalizedName.toString();
    }

    /**
     * Returns specification version of the given platform.
     *
     * @return instance of SpecificationVersion representing the version; never null
     */
    public static SpecificationVersion getSpecificationVersion(JavaPlatform plat) {
         String version = (String)plat.getSystemProperties().get("java.specification.version");   // NOI18N
         if (version == null) {
             version = "1.1";
         }
         return makeSpec(version);
    }


    public static FileObject findTool (String folder, String toolName, Collection<FileObject> installFolders) {
        assert toolName != null;
        for (FileObject root : installFolders ) {
            FileObject bin = root.getFileObject(folder);             //NOI18N
            if (bin == null) {
                continue;
            }
            String suffix = null;
            if (Utilities.isWindows()){ 
                suffix = toolName.indexOf('.') != -1 ? suffix : "exe";
                FileObject tool = bin.getFileObject(toolName, suffix);    //NOI18N
                if (tool!= null) {
                    return tool;
                }
                suffix = toolName.indexOf('.') != -1 ? suffix : "bat";
                tool = bin.getFileObject(toolName, suffix);    //NOI18N
                if (tool!= null) {
                    return tool;
                }
            } else {
                suffix = toolName.indexOf('.') != -1 ? suffix : null;
                FileObject tool = bin.getFileObject(toolName, suffix);    //NOI18N
                if (tool!= null) {
                    return tool;
                }
                suffix = toolName.indexOf('.') != -1 ? suffix : "sh";
                tool = bin.getFileObject(toolName, suffix);    //NOI18N
                if (tool!= null) {
                    return tool;
                }
            }
        }
        return null;
    }

    /**
     * Get JRE extension JARs/ZIPs.
     * @param extPath a native-format path for e.g. jre/lib/ext
     * @return a native-format classpath for extension JARs and ZIPs found in it
     */
    public static String getExtensions (String extPath) {
        if (extPath == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        StringTokenizer tk = new StringTokenizer (extPath, File.pathSeparator);
        while (tk.hasMoreTokens()) {
            File extFolder = FileUtil.normalizeFile(new File(tk.nextToken()));
            File[] files = extFolder.listFiles();
            if (files != null) {
                for (File f : files ) {
                    if (!f.exists()) {
                        //May happen, eg. broken link, it is safe to ignore it
                        //since it is an extension directory, but log it.
                        ErrorManager.getDefault().log (ErrorManager.WARNING,
                            MessageFormat.format (NbBundle.getMessage(Util.class,"MSG_BrokenExtension"),
                            new Object[] {f,extFolder}));
                        continue;
                    }                    
                    FileObject fo = FileUtil.toFileObject(f);
                    assert fo != null : "Must have defined a FileObject for existent file " + f;
                    if (!FileUtil.isArchiveFile(fo)) {
                        // #42961: Mac OS X has e.g. libmlib_jai.jnilib.
                        continue;
                    }
                    sb.append(File.pathSeparator);
                    sb.append(f.getAbsolutePath());
                }
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.substring(File.pathSeparator.length());
    }

    // copy pasted from org.openide.modules.Dependency:
    /** Try to make a specification version from a string.
     * Deal with errors gracefully and try to recover something from it.
     * E.g. "1.4.0beta" is technically erroneous; correct to "1.4.0".
     */
    private static SpecificationVersion makeSpec(String vers) {
        if (vers != null) {
            try {
                //platform supports only 1.2 version!
                return new SpecificationVersion("1.2");//vers);
            } catch (NumberFormatException nfe) {
                System.err.println("WARNING: invalid specification version: " + vers); // NOI18N
            }
            do {
                vers = vers.substring(0, vers.length() - 1);
                try {
                    return new SpecificationVersion(vers);
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            } while (vers.length() > 0);
        }
        // Nothing decent in it at all; use zero.
        return new SpecificationVersion("0"); // NOI18N
    }

    // XXX this method could probably be removed...
    private static boolean isArchiveFile (File f) {
        // the f might not exist and so you cannot use e.g. f.isFile() here
        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
    }

}
