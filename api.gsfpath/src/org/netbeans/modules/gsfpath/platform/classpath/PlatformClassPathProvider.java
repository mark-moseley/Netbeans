/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.gsfpath.platform.classpath;


import java.util.Collections;
import org.netbeans.spi.gsfpath.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.spi.gsfpath.classpath.ClassPathProvider;
import org.netbeans.api.gsfpath.platform.JavaPlatform;
import org.netbeans.api.gsfpath.platform.JavaPlatformManager;
import org.netbeans.spi.gsfpath.classpath.PathResourceImplementation;


public class PlatformClassPathProvider implements ClassPathProvider {



    /** Creates a new instance of PlatformClassPathProvider */
    public PlatformClassPathProvider() {
    }
    
    
    public ClassPath findClassPath(FileObject fo, String type) {
        if (fo == null || type == null) {
            throw new IllegalArgumentException();
        }
        JavaPlatform lp = this.getLastUsedPlatform(fo);
        JavaPlatform[] platforms;
        if (lp != null) {
            platforms = new JavaPlatform[] {lp};
        }
        else {
            JavaPlatformManager manager = JavaPlatformManager.getDefault();
            platforms = manager.getInstalledPlatforms();
        }
        for (int i=0; i<platforms.length; i++) {
            ClassPath bootClassPath = platforms[i].getBootstrapLibraries();
            ClassPath libraryPath = platforms[i].getStandardLibraries();
            ClassPath sourcePath = platforms[i].getSourceFolders();
            FileObject root = null;
            if (ClassPath.SOURCE.equals(type) && sourcePath != null &&
                (root = sourcePath.findOwnerRoot(fo))!=null) {
                this.setLastUsedPlatform (root,platforms[i]);
                return sourcePath;
            }
            else if (ClassPath.BOOT.equals(type) &&
                    ((bootClassPath != null && (root = bootClassPath.findOwnerRoot (fo))!=null) ||
                    (sourcePath != null && (root = sourcePath.findOwnerRoot(fo)) != null) ||
                    (libraryPath != null && (root = libraryPath.findOwnerRoot(fo))!=null))) {
                this.setLastUsedPlatform (root,platforms[i]);
                return bootClassPath;
            }
            else if (ClassPath.COMPILE.equals(type)) {
                if (libraryPath != null && (root = libraryPath.findOwnerRoot(fo))!=null) {
                    this.setLastUsedPlatform (root,platforms[i]);
                    return libraryPath;
                }
                else if ((bootClassPath != null && (root = bootClassPath.findOwnerRoot (fo))!=null) ||
                    (sourcePath != null && (root = sourcePath.findOwnerRoot(fo)) != null)) {
                    return this.getEmptyClassPath ();
                }
            }
        }
        return null;
    }

    private synchronized ClassPath getEmptyClassPath () {
        if (this.emptyCp == null ) {
            this.emptyCp = ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        }
        return this.emptyCp;
    }

    private synchronized void setLastUsedPlatform (FileObject root, JavaPlatform platform) {
        this.lastUsedRoot = root;
        this.lastUsedPlatform = platform;
    }

    private synchronized JavaPlatform getLastUsedPlatform (FileObject file) {
        if (this.lastUsedRoot != null && FileUtil.isParentOf(this.lastUsedRoot,file)) {
            return lastUsedPlatform;
        }
        else {
            return null;
        }
    }

    private FileObject lastUsedRoot;
    private JavaPlatform lastUsedPlatform;
    private ClassPath emptyCp;
}
