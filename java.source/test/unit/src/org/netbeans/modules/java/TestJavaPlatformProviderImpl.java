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
package org.netbeans.modules.java;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.platform.JavaPlatformProvider.class)
public class TestJavaPlatformProviderImpl implements JavaPlatformProvider {
    
    /** Creates a new instance of TestJavaPlatformProviderImpl */
    public TestJavaPlatformProviderImpl() {
    }

    public JavaPlatform[] getInstalledPlatforms() {
        return new JavaPlatform[] {getDefaultPlatform()};
    }

    private static DefaultPlatform DEFAULT = new DefaultPlatform();

    public JavaPlatform getDefaultPlatform() {
        return DEFAULT;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    private static final class DefaultPlatform extends JavaPlatform {
        private static ClassPath EMPTY = ClassPathSupport.createClassPath(Collections.EMPTY_LIST);

        public String getDisplayName() {
            return "default";
        }

        public Map getProperties() {
            return Collections.emptyMap();
        }

        private static ClassPath  bootClassPath;
        
        private static synchronized ClassPath getBootClassPath() {
            if (bootClassPath == null) {
                try {
                    String cp = System.getProperty("sun.boot.class.path");
                    List<URL> urls = new ArrayList<URL>();
                    String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));
                    
                    for (String path : paths) {
                        File f = new File(path);
                        
                        if (!f.canRead())
                            continue;
                        
                        FileObject fo = FileUtil.toFileObject(f);
                        
                        if (FileUtil.isArchiveFile(fo)) {
                            fo = FileUtil.getArchiveRoot(fo);
                        }
                        
                        if (fo != null) {
                            urls.add(fo.getURL());
                        }
                    }
                    
                    bootClassPath = ClassPathSupport.createClassPath((URL[])urls.toArray(new URL[0]));
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            
            return bootClassPath;
        }

        public ClassPath getBootstrapLibraries() {
            return getBootClassPath();
        }

        public ClassPath getStandardLibraries() {
            return EMPTY;
        }

        public String getVendor() {
            return "";
        }

        private Specification spec = new Specification("j2se", new SpecificationVersion("1.5"));

        public Specification getSpecification() {
            return spec;
        }

        public Collection getInstallFolders() {
            return Collections.emptyList();
        }

        public FileObject findTool(String toolName) {
            return null;//no tools supported.
        }

        public ClassPath getSourceFolders() {
            return EMPTY;
        }

        public List getJavadocFolders() {
            return Collections.emptyList();
        }

    }

}
