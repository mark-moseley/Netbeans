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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

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
        FileUtils.deleteRecursively(repoDir);
        TestKit.initRepo(repoDir, workDir);

        System.setProperty("svnClientAdapterFactory", "commandline");
        System.setProperty("netbeans.user", dataRootDir.getAbsolutePath());
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
                false);

        assertTrue(workDir.exists());
        assertEquals(3, workDir.list().length); // two folders + metadata

        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[] {""},
                workDir,
                username,
                password,
                false);

        assertTrue(workDir.exists());
        assertEquals(3, workDir.list().length); // two folders + metadata

        FileUtils.deleteRecursively(workDir);
        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[] {"folder1"},
                workDir,
                username,
                password,
                false);

        assertTrue(workDir.exists());
        assertEquals(1, workDir.list().length); // one folder

    }

    public void testCheckoutLocalLevel() throws MalformedURLException, SVNClientException {
        FileUtils.deleteRecursively(workDir);
        TestKit.mkdirs(repoDir, "testCheckoutLocalLevelfolder1/folder2/folder3");
        String url = TestUtilities.formatFileURL(repoDir) + "/testCheckoutLocalLevelfolder1/folder2";

        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                url,
                new String[0],
                workDir,
                username,
                password,
                true,
                true);

        assertTrue(workDir.exists());
        String[] files = workDir.list();
        assertEquals(2, files.length); // one folder + metadata

        String s = null;
        for (String f : files) {
            if(f.equals("folder3")) {
                s = f;
                break;
            }
        }
        assertEquals("folder3", s);
    }

    public void testMkdirMalformed() throws SVNClientException, IOException {
        MalformedURLException mue = null;
        try {
            org.netbeans.modules.subversion.api.Subversion.mkdir("crap", "", "", "creating dir");
        } catch (MalformedURLException e) {
            mue = e;
        }
        assertNotNull(mue);
    }

    public void testMkdir() throws SVNClientException, IOException {
        String url1 = TestUtilities.formatFileURL(repoDir) + "/foldertestMkdir";
        org.netbeans.modules.subversion.api.Subversion.mkdir(
                url1,
                "", "", "creating dir");
        ISVNInfo info = TestKit.getSVNInfo(url1);
        assertNotNull(info);

        String url2 = TestUtilities.formatFileURL(repoDir) + "/foldertestMkdir/foldertestMkdir2";
        org.netbeans.modules.subversion.api.Subversion.mkdir(
                url2,
                "", "", "creating dir");
        info = TestKit.getSVNInfo(url2);
        assertNotNull(info);
    }

    public void testCommit() throws SVNClientException, IOException {
        File folder = new File(workDir, "testCommitFolder");
        folder.mkdirs();
        TestKit.svnimport(repoDir, folder);

        ISVNStatus s = TestKit.getSVNStatus(folder);
        assertEquals(SVNStatusKind.NORMAL, s.getTextStatus());

        File file = new File(folder, "file");
        file.createNewFile();
        TestKit.add(file);
        s = TestKit.getSVNStatus(file);
        assertEquals(SVNStatusKind.ADDED, s.getTextStatus());

        Subversion.getInstance().versionedFilesChanged();
        SvnUtils.refreshParents(folder);
        Subversion.getInstance().getStatusCache().refreshRecursively(folder);

        org.netbeans.modules.subversion.api.Subversion.commit(new File[] {folder}, "", "", "msg");
        s = TestKit.getSVNStatus(file);
        assertEquals(SVNStatusKind.NORMAL, s.getTextStatus());

    }

}
