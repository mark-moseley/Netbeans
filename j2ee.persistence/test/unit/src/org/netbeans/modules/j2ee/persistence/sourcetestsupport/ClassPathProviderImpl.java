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

package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.io.Serializable;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * An implementation of ClassPathProvider for running tests. Includes <tt>toplink-essentials.jar</tt> that contains
 * <code>javax.persistence.*</code> stuff. 
 *
 * @author Erno Mononen
 */
public class ClassPathProviderImpl implements ClassPathProvider {
    
    private final ClassPath classPath;
    
    public ClassPathProviderImpl(FileObject[] sources){
        this.classPath = ClassPathSupport.createClassPath(sources);
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if(ClassPath.SOURCE.equals(type)){
            return this.classPath;
        }
        if (ClassPath.COMPILE.equals(type)){
            try {
                URL toplinkJarUrl = Class.forName("javax.persistence.EntityManager").getProtectionDomain().getCodeSource().getLocation();
                return ClassPathSupport.createClassPath(new URL[]{FileUtil.getArchiveRoot(toplinkJarUrl)});
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        if (ClassPath.BOOT.equals(type)){
            return JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        return null;
    }
}
