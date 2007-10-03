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

package org.netbeans.modules.j2ee.persistence.util;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Parameters;

/**
 * A helper class for ensuring that the class path is correctly
 * set for generating classes that require the Java Persistence API, such 
 * as entity classes.
 */
public class JPAClassPathHelper {
    
    private final Set<ClassPath> boot;
    private final Set<ClassPath> compile;
    private final Set<ClassPath> source;
    
    /**
     * Creates a new JPAClassPathHelper. 
     * 
     * @param boot the boot class paths. Must not be null.
     * @param compile the compile class paths. Must not be null.
     * @param source the source class paths. Must not be null.
     */ 
    public JPAClassPathHelper(Set<ClassPath> boot, Set<ClassPath> compile, Set<ClassPath> source){
        Parameters.notNull("boot", boot);
        Parameters.notNull("compile", compile);
        Parameters.notNull("source", source);
        this.boot = new HashSet<ClassPath>(boot);
        this.compile = new HashSet<ClassPath>(compile);
        this.source = new HashSet<ClassPath>(source);
    }
    
    /**
     * Creates a ClassPathInfo (based on our class paths) that can be used for generating entities.
     * Ensures that the compile class path has the Java Persistence API present by checking
     * whether JPA is already present in the compile class path and if it isn't, adds
     * an appropriate JPA library to the compile class path. It is the client's responsibility
     * to make sure that the IDE has a library that contains the Java Persistence API. If no
     * appropriate library could be found, an IllegalStateException is thrown. 
     * 
     * @return the ClassPathInfo for generating entities.
     * @throws IllegalStateException if there were no libraries in the IDE that 
     * contain the Java Persistence API.
     */ 
    public ClasspathInfo createClasspathInfo(){
        
        if (!ensureJPA()){
            throw new IllegalStateException("Cannot find a Java Persistence API library"); // NOI18N
        }
        
        return ClasspathInfo.create(
                createProxyClassPath(boot),
                createProxyClassPath(compile),
                createProxyClassPath(source)
                );
    }
    
    /**
     * Ensure that the compile class path has the Java Persistence API present. Checks
     * whether JPA is already present in the compile class path and if not, tries to 
     * find an appropriate JPA library and add it to the compile class path.
     * 
     * @return true if the compile class path contained or could be made to contain
     * the Java Persistence API.
     */  
    private boolean ensureJPA() {
        for (ClassPath classPath : compile) {
            if (classPath.findResource("javax/persistence/Entity.class") != null) { // NOI18N
                return true;
            }
        }
        ClassPath jpaClassPath = findJPALibrary();
        if (jpaClassPath != null) {
            compile.add(jpaClassPath);
            return true;
        }
        
        return false;
    }

    private ClassPath findJPALibrary() {
        Library library = PersistenceLibrarySupport.getFirstProviderLibrary();
        if (library == null) {
            return null;
        }
        List<URL> urls = library.getContent("classpath"); // NOI18N
        return ClassPathSupport.createClassPath(urls.toArray(new URL[urls.size()]));
    }
    
    
    private ClassPath createProxyClassPath(Set<ClassPath> classPaths) {
        return ClassPathSupport.createProxyClassPath(classPaths.toArray(new ClassPath[classPaths.size()]));
    }
}
