/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTest;
import java.io.File;
import java.io.InputStream;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class MoveTest extends AbstractCommandTest {
    
    public MoveTest(String testName) throws Exception {
        super(testName);
    }
    
    public void testMoveURL2URL() throws Exception {
        testMoveURL2URL("file", "filemove");
    }

    public void testMoveURL2URLWithAtSign() throws Exception {
        testMoveURL2URL("file1", "@filemove");
        testMoveURL2URL("file2", "file@move");
        testMoveURL2URL("file3", "filemove@");
    }

    public void testMoveURLWithAtSign2URL() throws Exception {
        testMoveURL2URL("@file", "filemove1");
        testMoveURL2URL("fi@le", "filemove2");
        testMoveURL2URL("file@", "filemove3");
    }

    public void testMoveURL2URLInDir() throws Exception {
        testMoveURL2URL("folder/file", "filemove");
    }

    public void testMoveURL2URLWithAtSignInDir() throws Exception {
        testMoveURL2URL("folder/file1", "@filemove");
        testMoveURL2URL("folder/file2", "file@move");
        testMoveURL2URL("folder/file3", "filemove@");
    }

    public void testMoveURLWithAtSign2URLInDir() throws Exception {
        testMoveURL2URL("folder/@file", "filemove1");
        testMoveURL2URL("folder/fi@le", "filemove2");
        testMoveURL2URL("folder/file@", "filemove3");
    }

    private void testMoveURL2URL(String srcPath, String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);
                
        File filemove = createFile(renameFile(srcPath, targetFileName));
        
        ISVNClientAdapter c = getNbClient();
        c.move(getFileUrl(file), getFileUrl(filemove), "move", SVNRevision.HEAD);

        ISVNInfo info = null;
        SVNClientException ex = null;
        try {
            getInfo(getFileUrl(file));
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(info);
        assertNotNull(ex);
        
        info = getInfo(getFileUrl(filemove));
        assertNotNull(info);
        assertEquals(getFileUrl(filemove), TestUtilities.decode(info.getUrl()));
        
        assertNotifiedFiles(new File[] {});        
    }        
    
    public void testMoveURL2URLPrevRevision() throws Exception {
        testMoveURL2URLPrevRevision("file", "filemove");
    }

    public void testMoveURL2URLWithAtSignPrevRevision() throws Exception {
        testMoveURL2URLPrevRevision("file1", "@filemove");
        testMoveURL2URLPrevRevision("file2", "file@move");
        testMoveURL2URLPrevRevision("file3", "filemove@");
    }

    public void testMoveURLWithAtSign2URLPrevRevision() throws Exception {
        testMoveURL2URLPrevRevision("@file", "filemove1");
        testMoveURL2URLPrevRevision("fi@le", "filemove2");
        testMoveURL2URLPrevRevision("file@", "filemove3");
    }

    private void testMoveURL2URLPrevRevision(String srcPath,
                                             String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        write(file, 1);
        add(file);
        commit(file);
        SVNRevision prevRev = getRevision(file);
        write(file, 2);
        commit(getWC());        
        
        File filemove = createFile(renameFile(srcPath, targetFileName));
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filemove), "move", prevRev);

        ISVNLogMessage[] logs = getLog(getFileUrl(filemove));
        assertEquals(((SVNRevision.Number)prevRev).getNumber() ,logs[0].getChangedPaths()[0].getCopySrcRevision().getNumber());

        InputStream is = getContent(getFileUrl(filemove));
        assertContents(is, 1);
        assertNotifiedFiles(new File[] {});        
    }           
    
    public void testMoveFile2File() throws Exception {
        testMoveFile2File("file", "filemove");
    }

    public void testMoveFileWithAtSign2File() throws Exception {
        testMoveFile2File("@file", "filemove1");
        testMoveFile2File("fi@le", "filemove2");
        testMoveFile2File("file@", "filemove3");
    }

    public void testMoveFile2FileWithAtSign() throws Exception {
        testMoveFile2File("file1", "@filemove");
        testMoveFile2File("file2", "file@move");
        testMoveFile2File("file3", "filemove@");
    }

    public void testMoveFile2FileInDir() throws Exception {
        testMoveFile2File("folder/file", "filemove");
    }

    public void testMoveFileWithAtSign2FileInDir() throws Exception {
        testMoveFile2File("folder/@file", "filemove1");
        testMoveFile2File("folder/fi@le", "filemove2");
        testMoveFile2File("folder/file@", "filemove3");
    }

    public void testMoveFile2FileWithAtSignInDir() throws Exception {
        testMoveFile2File("folder/file1", "@filemove");
        testMoveFile2File("folder/file2", "file@move");
        testMoveFile2File("folder/file3", "filemove@");
    }

    private void testMoveFile2File(String srcPath, String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);
                
        File filemove = new File(getWC(), renameFile(srcPath, targetFileName));
        
        ISVNClientAdapter c = getNbClient();
        c.move(file, filemove, true);

        assertTrue(filemove.exists());
        assertStatus(SVNStatusKind.ADDED, filemove);
        assertNotifiedFiles(new File[] {file, filemove});                
    }        
    
}
