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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.URLMapper;

/**
 * Defines Javadoc locations for built modules.
 * @author Jesse Glick
 */
public final class JavadocForBinaryImpl implements JavadocForBinaryQueryImplementation {
    
    private static final String NB_ALL_INFIX = "nbbuild" + File.separatorChar + "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    private static final String EXT_INFIX = "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    
    /** Configurable for the unit test, since it is too cumbersome to create fake Javadoc in all the right places. */
    static boolean ignoreNonexistentRoots = true;
    
    private final NbModuleProject project;
    
    public JavadocForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }

    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        if (!binaryRoot.equals(Util.urlForJar(project.getModuleJarLocation()))) {
            return null;
        }
        String cnb = project.getCodeNameBase();
        if (cnb == null) { // #115521
            return null;
        }
        String cnbdashes = cnb.replace('.', '-');
        try {
            final List<URL> candidates = new ArrayList<URL>();
            NbPlatform platform = project.getPlatform(false);
            if (platform == null) {
                return null;
            }
            for (URL root : platform.getJavadocRoots()) {
                candidates.add(new URL(root, cnbdashes + "/")); // NOI18N
            }
            File dir;
            NbModuleProvider.NbModuleType type = Util.getModuleType(project);
            if (type == NbModuleProvider.NETBEANS_ORG) {
                dir = project.getNbrootFile(NB_ALL_INFIX + cnbdashes);
            } else {
                dir = new File(project.getProjectDirectoryFile(), EXT_INFIX + cnbdashes);
            }
            if (dir != null) { // #118491
                candidates.add(Util.urlForDir(dir));
            }
            if (ignoreNonexistentRoots) {
                Iterator<URL> it = candidates.iterator();
                while (it.hasNext()) {
                    URL u = it.next();
                    if (URLMapper.findFileObject(u) == null) {
                        it.remove();
                    }
                }
            }
            return new R(candidates.toArray(new URL[candidates.size()]));
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private static final class R implements JavadocForBinaryQuery.Result {
        private final URL[] roots;
        public R(URL[] roots) {
            this.roots = roots;
        }
        public URL[] getRoots() {
            return roots;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }

}
