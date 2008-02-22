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
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.test.TestFileUtils;

/**
 * Test functionality of GlobalSourceForBinaryImpl.
 *
 * @author Martin Krauskopf
 */
public class GlobalSourceForBinaryImplTest extends TestBase {
    
    // Doesn't need to be precise and/or valid. Should show what actual
    // GlobalSourceForBinaryImpl works with.
    private static final String LOADERS_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
            "<type>org.netbeans.modules.apisupport.project</type>\n" +
            "<configuration>\n" +
            "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3\">\n" +
            "<code-name-base>org.openide.loaders</code-name-base>\n" +
            "</data>\n" +
            "</configuration>\n" +
            "</project>\n";
    
    public GlobalSourceForBinaryImplTest(String name) {
        super(name);
    }

    public void testFindSourceRootForZipWithFirstLevelDepthNbBuild() throws Exception {
        File nbSrcZip = generateNbSrcZip("");
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    public void testFindSourceRootForZipWithSecondLevelDepthNbBuild() throws Exception {
        File nbSrcZip = generateNbSrcZip("netbeans-src/");
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "netbeans-src/openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    // just sanity check that exception is not thrown
    public void testBehaviourWithNonZipFile() throws Exception {
        GlobalSourceForBinaryImpl.quiet = true;
        File nbSrcZip = new File(getWorkDir(), "wrong-nbsrc.zip");
        nbSrcZip.createNewFile();
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots();
    }
    
    public void testResolveSpecialNBSrcPaths() throws Exception {
        String[] srcDirs = {"xtest/nbjunit/src",
                            "xtest/nbjunit/ide/src",
                            "o.n.insane/src"};
        String[] xtestJars = {"xtest/lib/nbjunit.jar",
                            "xtest/lib/nbjunit-ide.jar",
                            "xtest/lib/insanelib.jar"};
        String[] ideJars = {"testtools/modules/org-netbeans-modules-nbjunit.jar",
                            "testtools/modules/org-netbeans-modules-nbjunit-ide.jar",
                            "testtools/modules/ext/insanelib.jar"};
        for (int i = 0; i < srcDirs.length; i++) {
            if(!file(srcDirs[i]).isDirectory()) {
                System.err.println("Skipping testResolveSpecialNBSrcPaths since "+srcDirs[i]+" is not checked out");
                continue;
            }
            assertRoot(Util.urlForJar(file(xtestJars[i])),
                    FileUtil.toFileObject(file(srcDirs[i])));
            File jarFile = new File(file("nbbuild/netbeans"), ideJars[i]);
            if (jarFile.exists()) {
                assertResolved(ideJars[i], srcDirs[i]);
            } else {
                assertEquals("no resolved root", 0,
                        SourceForBinaryQuery.findSourceRoots(Util.urlForJar(jarFile)).getRoots().length);
            }
        }
    }
    
    private void assertResolved(String jarInNBBuild, String dirInNBSrc) {
        File jarFile = new File(file("nbbuild/netbeans"), jarInNBBuild);
        assertRoot(Util.urlForJar(jarFile), FileUtil.toFileObject((file(dirInNBSrc))));
    }
    
    public void testListeningToNbPlatform() throws Exception {
        File nbSrcZip = generateNbSrcZip("");
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(loadersURL);
        assertNotNull("got result", res);
        ResultChangeListener resultCL = new ResultChangeListener();
        res.addChangeListener(resultCL);
        assertFalse("not changed yet", resultCL.changed);
        assertEquals("non source root", 0, res.getRoots().length);
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        assertTrue("changed yet", resultCL.changed);
        assertEquals("one source root", 1, res.getRoots().length);
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    private File generateNbSrcZip(String topLevelEntry) throws IOException {
        return FileUtil.toFile(TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "nbsrc.zip",
                topLevelEntry + "nbbuild/nbproject/project.xml:",
                topLevelEntry + "openide/loaders/src/dummy:",
                topLevelEntry + "openide/loaders/nbproject/project.xml:" + LOADERS_XML));
    }
    
    private static void assertRoot(final URL loadersURL, final FileObject loadersSrcFO) {
        assertEquals("right results for " + loadersURL,
                Collections.singletonList(loadersSrcFO),
                Arrays.asList(SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots()));
    }
    
    private static final class ResultChangeListener implements ChangeListener {
        
        private boolean changed;
        
        public void stateChanged(ChangeEvent e) {
            changed = true;
        }
        
    }
    
}
