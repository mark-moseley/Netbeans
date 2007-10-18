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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.web.project.SourceRoots;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for a web project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    private final ClassPath[] cache = new ClassPath[10];

    private final Map<String,FileObject> dirCache = new HashMap<String,FileObject>();

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testSourceRoots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    private synchronized FileObject getDir(String propname) {
        FileObject fo = (FileObject) this.dirCache.get (propname);
        if (fo == null ||  !fo.isValid()) {
            String prop = evaluator.getProperty(propname);
            if (prop != null) {
                fo = helper.resolveFileObject(prop);
                this.dirCache.put (propname, fo);
            }
        }
        return fo;
    }
    
    private FileObject[] getPrimarySrcPath() {
        return this.sourceRoots.getRoots();
    }
    
    private FileObject[] getTestSrcDir() {
        return this.testSourceRoots.getRoots();
    }

    private FileObject getBuildClassesDir() {
        return getDir(WebProjectProperties.BUILD_CLASSES_DIR);
    }
    
    private FileObject getDistJar() {
        return getDir(WebProjectProperties.DIST_WAR);
    }
    
    private FileObject getBuildTestClassesDir() {
        return getDir(WebProjectProperties.BUILD_TEST_CLASSES_DIR);
    }

    private FileObject getDocumentBaseDir() {
        return getDir(WebProjectProperties.WEB_DOCBASE_DIR);
    }
    
     /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>test source</dd>
     *         <dt>2</dt> <dd>built class (unpacked)</dd>
     *         <dt>3</dt> <dd>built test class</dd>
     *         <dt>4</dt> <dd>built class (in dist JAR)</dd>
     *         <dt>5</dt> <dd>web pages</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
   private int getType(FileObject file) {
        FileObject[] srcPath = getPrimarySrcPath();
        for (int i=0; i < srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 0;
            }
        }        
        srcPath = getTestSrcDir();
        for (int i=0; i< srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 1;
            }
        }
        FileObject dir = getDocumentBaseDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return 5;
        }
        dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 2;
        }
        dir = getDistJar(); // not really a dir at all, of course
        if (dir != null && dir.equals(FileUtil.getArchiveFile(file))) {
            // XXX check whether this is really the root
            return 4;
        }
        dir = getBuildTestClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return 3;
        }
        
        return -1;
    }
    
    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        return this.getCompileTimeClasspath(type);
    }
    
    private synchronized ClassPath getCompileTimeClasspath(int type) {        
        if ((type < 0 || type > 2) && type != 5) {
            // Not a source file.
            return null;
        }
        if (type == 2 || type == 5)
            type = 0;
        
        ClassPath cp = cache[3+type];
        if ( cp == null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "${javac.classpath}:${" 
                        + WebProjectProperties.J2EE_PLATFORM_CLASSPATH 
                        + "}", evaluator, false));      //NOI18N
            }
            else {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "${javac.test.classpath}:${" 
                        + WebProjectProperties.J2EE_PLATFORM_CLASSPATH 
                        + "}", evaluator, false));      //NOI18N
            }
            cache[3+type] = cp;
        }
        return cp;
        
    }
    
    private synchronized ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 5) {
            // Unregistered file, or in a JAR.
            // For jar:file:$projdir/dist/*.jar!/**/*.class, it is misleading to use
            // run.classpath since that does not actually contain the file!
            // (It contains file:$projdir/build/classes/ instead.)
            return null;
        }
        switch (type){
            case 2:
            case 3:
            case 4: type -= 2; break;
            case 5: type = 0; break;
        }
        
        ClassPath cp = cache[6+type];
        if ( cp == null ) {
            if (type == 0) {
                //XXX : It should return a classpath for run.classpath property, but
                // the run.classpath property was removed from the webproject in the past
                // and I'm a little lazy to return it back in the code:)). In this moment
                // the run classpath equals to the debug classpath. If the debug classpath
                // will be different from the run classpath, then the run classpath should
                // be returned back. 
                ClassPath debugClassPath = ClassPathFactory.createClassPath(
                        new ProjectClassPathImplementation(
                                helper, WebProjectProperties.DEBUG_CLASSPATH, evaluator));
                ClassPath j2eePlatformClassPath = ClassPathFactory.createClassPath(
                        new ProjectClassPathImplementation(
                                helper, WebProjectProperties.J2EE_PLATFORM_CLASSPATH, evaluator));
                
                cp = ClassPathSupport.createProxyClassPath(debugClassPath, j2eePlatformClassPath);
                
            }
            cache[6+type] = cp;
        }
        return cp;
    }
    
    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }
    
    private synchronized ClassPath getSourcepath(int type) {
        if ((type < 0 || type > 2) && type != 5) {
            // Unknown.
            return null;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            switch (type) {
                case 0:
                case 2:    
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation (this.sourceRoots,helper));
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation (this.testSourceRoots, helper));
                    break;
                case 5:
                    cp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                        ClassPathFactory.createClassPath(new JspSourcePathImplementation(helper, evaluator)),
                        ClassPathFactory.createClassPath(new SourcePathImplementation (this.sourceRoots, helper)),
                    });
                    break;
            }
            cache[type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[7];
        if (cp == null ) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(evaluator));
            cache[7] = cp;
        }
        return cp;
    }
    
    public synchronized ClassPath getJ2eePlatformClassPath() {
        ClassPath cp = cache[9];
        if (cp == null) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,  "${" + //NOI18N
                        WebProjectProperties.J2EE_PLATFORM_CLASSPATH  +  
                        "}", evaluator, false));  //NOI18N
            cache[9] = cp;
        }
        return cp;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else {
            return null;
        }
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[]{getBootClassPath()};
        }
        if (ClassPath.COMPILE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getCompileTimeClasspath(0);
            l[1] = getCompileTimeClasspath(1);
            return l;
        }
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[3];
            l[0] = getSourcepath(0);
            l[1] = getSourcepath(5);
            l[2] = getSourcepath(1);
            return l;
        }
        assert false;
        return null;
    }
    
    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return getBootClassPath();
        }
        if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(0);
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        assert false;
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
    
    public String getPropertyName (SourceGroup sg, String type) {
        FileObject root = sg.getRootFolder();
        FileObject[] path = getPrimarySrcPath();
        for (int i=0; i<path.length; i++) {
            if (root.equals(path[i])) {
                if (ClassPath.COMPILE.equals(type)) {
                    return WebProjectProperties.JAVAC_CLASSPATH;
                }
                else if (ClassPath.EXECUTE.equals(type)) {
                    return WebProjectProperties.DEBUG_CLASSPATH;
                }
                else {
                    return null;
                }
            }
        }
        path = getTestSrcDir();
        for (int i=0; i<path.length; i++) {
            if (root.equals(path[i])) {
                if (ClassPath.COMPILE.equals(type)) {
                    return WebProjectProperties.JAVAC_TEST_CLASSPATH;
                }
                else if (ClassPath.EXECUTE.equals(type)) {
                    return WebProjectProperties.RUN_TEST_CLASSPATH;
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }

}

