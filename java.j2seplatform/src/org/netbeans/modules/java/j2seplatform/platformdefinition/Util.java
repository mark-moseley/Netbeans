/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import java.util.*;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Utilities;

public class Util {

    private Util () {
    }

    static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List/*<PathResourceImplementation>*/ list = new ArrayList();
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
                //Hotfix issue 42961
                return null;
                //url = f.toURI().toURL();
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

    /**
     * Process a list of URLs, producing the archive root for archive file URLs.
     * @param archives a list of URLs which may include some archive files and some not
     * @return a similar list of URLs, with the archive root used in place of each archive file
     */
    public static List/*<URL>*/ getResourcesRoots(List/*<URL>*/ archives) {
        assert archives != null;
        List/*<URL>*/ result = new ArrayList ();
        for (Iterator it = archives.iterator(); it.hasNext();) {
            URL url = (URL) it.next();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            result.add (url);
        }
        return result;
    }


    public static FileObject findTool (String toolName, Collection installFolders) {
        assert toolName != null;
        for (Iterator it = installFolders.iterator(); it.hasNext();) {
            FileObject root = (FileObject) it.next();
            FileObject bin = null;
            switch (Utilities.getOperatingSystem()) {
                case Utilities.OS_MAC:
                    bin = root.getFileObject("Commands");        //NOI18N
                    break;
                default:
                    bin = root.getFileObject("bin");             //NOI18N
            }
            if (bin == null) {
                continue;
            }
            FileObject tool = bin.getFileObject(toolName, Utilities.isWindows() ? "exe" : null);    //NOI18N
            if (tool!= null) {
                return tool;
            }
        }
        return null;
    }

    public static String getExtensions (String extPath) {
        if (extPath == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        StringTokenizer tk = new StringTokenizer (extPath, File.pathSeparator);
        while (tk.hasMoreTokens()) {
            File extFolder = new File(tk.nextToken());
            File[] files = extFolder.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    sb.append(File.pathSeparator);
                    sb.append(files[i].getAbsolutePath());
                }
            }
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
                return new SpecificationVersion(vers);
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
