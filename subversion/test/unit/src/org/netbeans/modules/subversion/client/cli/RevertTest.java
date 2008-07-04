/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.subversion.client.cli;

import java.io.File;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class RevertTest extends AbstractCLITest {
    
    public RevertTest(String testName) throws Exception {
        super(testName);
    }
            
    public void testRevert() throws Exception {                                                
        File file = createFile("file");
        write(file, 1);
        add(file);
        commit(file);
        assertStatus(SVNStatusKind.NORMAL, file);
        write(file, 2);
        assertStatus(SVNStatusKind.MODIFIED, file);
        
        ISVNClientAdapter c = getNbClient();        
        c.revert(file, false);

        assertStatus(SVNStatusKind.NORMAL, file);
        assertContents(file, 1);
        assertNotifiedFiles(new File[] {file});        
    }            
    
    public void testRevertNoFiles() throws Exception {                                                
        ISVNClientAdapter c = getNbClient();        
        SVNClientException e1 = null;
        try {
            c.revert(null, false);
        } catch (SVNClientException e) {
            e1 = e;
        }
        assertNull(e1);
    }            
    
    public void testRevertFiles() throws Exception {                                                
        File file1 = createFile("file1");
        File file2 = createFile("file2");
        File file3 = createFile("file3");
        write(file1, 1);
        write(file2, 1);
        write(file3, 1);
        add(file1);
        add(file2);
        add(file3);
        commit(file1);
        commit(file2);
        commit(file3);
        
        assertStatus(SVNStatusKind.NORMAL, file1);
        assertStatus(SVNStatusKind.NORMAL, file2);
        assertStatus(SVNStatusKind.NORMAL, file3);
        
        write(file1, 2);
        write(file2, 2);
        write(file3, 2);
        
        assertStatus(SVNStatusKind.MODIFIED, file1);
        assertStatus(SVNStatusKind.MODIFIED, file2);
        assertStatus(SVNStatusKind.MODIFIED, file3);
        
        ISVNClientAdapter c = getNbClient();        
        for(File f : new File[] {file1, file2, file3}) {
            c.revert(f, false);
        }

        assertStatus(SVNStatusKind.NORMAL, file1);
        assertStatus(SVNStatusKind.NORMAL, file2);
        assertStatus(SVNStatusKind.NORMAL, file3);
        assertContents(file1, 1);
        assertContents(file2, 1);
        assertContents(file3, 1);
        assertNotifiedFiles(new File[] {file1, file2, file3});        
    }            
    
    public void testRevertFolderRecursivelly() throws Exception {                                                
        File folder1 = createFolder("folder1");
        File file11 = createFile(folder1, "file11");
        File folder11 = createFolder(folder1, "folder11");
        File file111 = createFile(folder11, "file111");
        
        write(file11, 1);
        write(file111, 1);        
        add(folder1);
        add(file11);
        add(folder11);
        add(file111);
        commit(folder1);
        commit(file11);
        commit(folder11);
        commit(file111);
        
        assertStatus(SVNStatusKind.NORMAL, folder1);
        assertStatus(SVNStatusKind.NORMAL, file11);
        assertStatus(SVNStatusKind.NORMAL, folder11);
        assertStatus(SVNStatusKind.NORMAL, file111);
        
        write(file11, 2);
        write(file111, 2);
        
        assertStatus(SVNStatusKind.MODIFIED, file11);
        assertStatus(SVNStatusKind.MODIFIED, file111);        
        
        ISVNClientAdapter c = getNbClient();        
        for(File f : new File[] {folder1}) {
            c.revert(f, true);
        }

        assertStatus(SVNStatusKind.NORMAL, file11);
        assertStatus(SVNStatusKind.NORMAL, file111);
        assertStatus(SVNStatusKind.NORMAL, folder1);
        assertStatus(SVNStatusKind.NORMAL, folder11);
        assertContents(file11, 1);
        assertContents(file111, 1);        
        assertNotifiedFiles(new File[] {file11, file111});       // only files were reverted (modified) 
    }        
    
    public void testRevertFolderNonRecursivelly() throws Exception {                                                
        File folder1 = createFolder("folder1");
        File file11 = createFile(folder1, "file11");
        File folder11 = createFolder(folder1, "folder11");
        File file111 = createFile(folder11, "file111");
        
        write(file11, 1);
        write(file111, 1);        
        add(folder1);
        add(file11);
        add(folder11);
        add(file111);
        commit(folder1);
        commit(file11);
        commit(folder11);
        commit(file111);
        
        assertStatus(SVNStatusKind.NORMAL, folder1);
        assertStatus(SVNStatusKind.NORMAL, file11);
        assertStatus(SVNStatusKind.NORMAL, folder11);
        assertStatus(SVNStatusKind.NORMAL, file111);
        
        write(file11, 2);
        write(file111, 2);
        
        assertStatus(SVNStatusKind.MODIFIED, file11);
        assertStatus(SVNStatusKind.MODIFIED, file111);        
        
        ISVNClientAdapter c = getNbClient();        
        for(File f : new File[] {folder1}) {
            c.revert(f, true);
        }

        assertStatus(SVNStatusKind.NORMAL, file11);
        assertStatus(SVNStatusKind.NORMAL, file111);
        assertStatus(SVNStatusKind.NORMAL, folder1);
        assertStatus(SVNStatusKind.NORMAL, folder11);
        assertContents(file11, 1);
        assertContents(file111, 1);        
        assertNotifiedFiles(new File[] {file11, file111}); // only files were reverted (modified)       
    }            
    
    public void testRevertFolders() throws Exception {                                                
        File folder1 = createFolder("folder1");
        File file1 = createFile(folder1, "file11");
        
        File folder2 = createFolder("folder11");
        File file2 = createFile(folder2, "file111");
        
        write(file1, 1);
        write(file2, 1);        
        add(folder1);
        add(file1);
        add(folder2);
        add(file2);
        commit(folder1);
        commit(file1);
        commit(folder2);
        commit(file2);
        
        assertStatus(SVNStatusKind.NORMAL, folder1);
        assertStatus(SVNStatusKind.NORMAL, file1);
        assertStatus(SVNStatusKind.NORMAL, folder2);
        assertStatus(SVNStatusKind.NORMAL, file1);
        
        write(file1, 2);
        write(file2, 2);
        
        assertStatus(SVNStatusKind.MODIFIED, file1);
        assertStatus(SVNStatusKind.MODIFIED, file2);        
        
        ISVNClientAdapter c = getNbClient();        
        for(File f : new File[] {folder1, folder2}) {
            c.revert(f, true);
        }

        assertStatus(SVNStatusKind.NORMAL, file1);
        assertStatus(SVNStatusKind.NORMAL, file2);
        assertStatus(SVNStatusKind.NORMAL, folder1);
        assertStatus(SVNStatusKind.NORMAL, folder2);
        assertContents(file1, 1);
        assertContents(file2, 1);        
        assertNotifiedFiles(new File[] {file1, file2});  // only files were reverted (modified)      
    }        
    
}
