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

package org.netbeans.modules.j2ee.ejbcore.test;

import java.net.URL;
import javax.ejb.Stateless;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class ClassPathProviderImpl implements ClassPathProvider {
    
    private ClassPath sourcePath;
    private final ClassPath compilePath;
    private final ClassPath bootPath;
    
    public ClassPathProviderImpl() {
        URL statelessAnnotationURL = Stateless.class.getProtectionDomain().getCodeSource().getLocation();
        this.compilePath = ClassPathSupport.createClassPath(new URL[] { FileUtil.getArchiveRoot(statelessAnnotationURL) });
        this.bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (ClassPath.SOURCE.equals(type)) {
            return sourcePath;
        } else if (ClassPath.COMPILE.equals(type)) {
            return compilePath;
        } else if (ClassPath.BOOT.equals(type)) {
            return bootPath;
        }
        return null;
    }
    
    public void setClassPath(FileObject[] sources) {
        sourcePath = ClassPathSupport.createClassPath(sources);
    }
    
}
