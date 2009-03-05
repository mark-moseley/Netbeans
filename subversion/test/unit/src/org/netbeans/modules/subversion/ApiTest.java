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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author Tomas Stupka
 */
public class ApiTest extends NbTestCase {
    private String username;
    private String password;
    private File workDir;
    private File dataRootDir;
    private File repoDir;
          
    public ApiTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
        username = br.readLine();
        password = br.readLine();
        br.close();
        workDir = FileUtils.createTmpFolder("svncoapi");

        dataRootDir = new File(System.getProperty("data.root.dir"));
        repoDir = new File(dataRootDir, "repo");
        TestKit.initRepo(repoDir, workDir);

        System.setProperty("svnClientAdapterFactory", "commandline");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteRecursively(workDir);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testCheckout() throws MalformedURLException, SVNClientException {
        FileUtils.deleteRecursively(workDir);
        // XXX use onw repo
        TestKit.mkdirs(repoDir, "folder1");
        TestKit.mkdirs(repoDir, "folder2");

        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[0],
                workDir,
                username,
                password,
                true);

        assertTrue(workDir.exists());
        assertTrue(workDir.list().length == 2);

        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[] {""},
                workDir,
                username,
                password,
                true);

        assertTrue(workDir.exists());
        assertTrue(workDir.list().length == 2);

        FileUtils.deleteRecursively(workDir);
        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[] {"folder1"},
                workDir,
                username,
                password,
                true);

        assertTrue(workDir.exists());
        assertTrue(workDir.list().length == 1);

    }
    
    


}
