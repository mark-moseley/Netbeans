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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;

/**
 * Able to find Javadoc in the appropriate NbPlatform for the given URL.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
public final class GlobalJavadocForBinaryImpl implements JavadocForBinaryQueryImplementation {
    
    public JavadocForBinaryQuery.Result findJavadoc(final URL root) {
        try {
            if (root.getProtocol().equals("jar")) { // NOI18N
                return findForBinaryRoot(root);
            } else {
                return findForSourceRoot(root);
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private Result findForBinaryRoot(final URL binaryRoot) throws MalformedURLException, MalformedURLException {
        URL jar = FileUtil.getArchiveFile(binaryRoot);
        if (!jar.getProtocol().equals("file")) { // NOI18N
            Util.err.log(binaryRoot + " is not an archive file."); // NOI18N
            return null;
        }
        if (jar.toExternalForm().endsWith("/xtest/lib/junit.jar")) { // NOI18N
            // #68685 hack - associate reasonable Javadoc with XTest's version of junit
            File f = InstalledFileLocator.getDefault().locate("modules/ext/junit-3.8.2.jar", "org.netbeans.modules.junit", false); // NOI18N
            if (f == null) {
                // For compat with NB 5.0.
                f = InstalledFileLocator.getDefault().locate("modules/ext/junit-3.8.1.jar", "org.netbeans.modules.junit", false); // NOI18N
            }
            if (f != null) {
                return JavadocForBinaryQuery.findJavadoc(FileUtil.getArchiveRoot(f.toURI().toURL()));
            }
        }
        File binaryRootF = new File(URI.create(jar.toExternalForm()));
        // XXX this will only work for modules following regular naming conventions:
        String n = binaryRootF.getName();
        if (!n.endsWith(".jar")) { // NOI18N
            Util.err.log(binaryRootF + " is not a *.jar"); // NOI18N
            return null;
        }
        String cnbdashes = n.substring(0, n.length() - 4);
        NbPlatform supposedPlaf = null;
        for (NbPlatform plaf : NbPlatform.getPlatforms()) {
            if (binaryRootF.getAbsolutePath().startsWith(plaf.getDestDir().getAbsolutePath())) {
                supposedPlaf = plaf;
                break;
            }
        }
        if (supposedPlaf == null) {
            Util.err.log(binaryRootF + " does not correspond to a known platform"); // NOI18N
            return null;
        }
        return findByDashedCNB(cnbdashes, supposedPlaf);
    }

    /**
     * Go through all registered platforms and tries to find Javadoc for the
     * given URL.
     * <p>
     * <em>TODO</em>: ideally should check module, or at least cluster, version.
     */
    private Result findForSourceRoot(final URL root) throws MalformedURLException {
        Project p ;
        try {
            p = FileOwnerQuery.getOwner(root.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        if (p != null) {
            NbModuleProject module = p.getLookup().lookup(NbModuleProject.class);
            if (module != null) {
                String cnb = module.getCodeNameBase();
                for (NbPlatform plaf : NbPlatform.getPlatforms()) {
                    Result r = findByDashedCNB(cnb.replace('.', '-'), plaf);
                    if (r != null) {
                        return r;
                    }
                }
            }
        }
        return null;
    }
   
    private Result findByDashedCNB(final String cnbdashes, final NbPlatform plaf) throws MalformedURLException {
        final List<URL> candidates = new ArrayList<URL>();
        URL[] roots = plaf.getJavadocRoots();
        Util.err.log("Platform in " + plaf.getDestDir() + " claimed to have Javadoc roots " + Arrays.asList(roots));
        for (URL root : roots) {
            // XXX: so should be checked, instead of always adding both?
            // 1. user may insert directly e.g ...nbbuild/build/javadoc/org-openide-actions[.zip]
            candidates.add(root);
            // 2. or whole bunch of javadocs e.g. ...nbbuild/build/javadoc/
            candidates.add(new URL(root, cnbdashes + '/'));
        }
        Iterator<URL> it = candidates.iterator();
        while (it.hasNext()) {
            URL u = it.next();
            if (URLMapper.findFileObject(u) == null) {
                Util.err.log("No such Javadoc candidate URL " + u);
                it.remove();
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return new JavadocForBinaryQuery.Result() {
            public URL[] getRoots() {
                return candidates.toArray(new URL[candidates.size()]);
            }
            public void addChangeListener(ChangeListener l) {
            }
            public void removeChangeListener(ChangeListener l) {
            }
        };
    }

}
