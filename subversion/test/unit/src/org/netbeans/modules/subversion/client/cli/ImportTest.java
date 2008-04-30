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
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class ImportTest extends AbstractCLITest {
    
    public ImportTest(String testName) throws Exception {
        super(testName);
    }
    
    public void testImportFile() throws Exception {                                        
        File file = createFile("file");
                
        assertTrue(file.exists());
        
        ISVNClientAdapter c = getReferenceClient();
        SVNUrl url = getRepoUrl().appendPath(getName()).appendPath(file.getName());
        c.doImport(file, url, "imprd", false);

        assertTrue(file.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, file);

        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), info.getUrl().toString());
    }            
    
    public void testImportFolder() throws Exception {                                        
        File folder = createFolder("folder");
                
        assertTrue(folder.exists());
        
        ISVNClientAdapter c = getNbClient();
        SVNUrl url = getTestUrl().appendPath(getName()); 
        c.mkdir(url, "mrkvadir");        
        url = url.appendPath(folder.getName());
        c.mkdir(url, "mrkvadir");        
        c.doImport(folder, url, "imprd", false);

        assertTrue(folder.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        
        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), info.getUrl().toString());
    }
    
    public void testImportFolderNonRecursivelly() throws Exception {                                        
        File folder = createFolder("folder");
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder1, "file");
                
        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        
        ISVNClientAdapter c = getNbClient();
        SVNUrl url = getTestUrl().appendPath(getName());
        c.mkdir(url, "mrkvadir");        
        url = url.appendPath(folder.getName());
        c.mkdir(url, "mrkvadir");        
        c.doImport(folder, url, "imprd", false);

        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        assertStatus(SVNStatusKind.UNVERSIONED, folder1);
        assertStatus(SVNStatusKind.UNVERSIONED, file);
        
        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), info.getUrl().toString());
        
        SVNClientException ex = null;
        try {
            info = getInfo(url.appendPath(folder1.getName()));
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNotNull(ex);
    }
    
    public void testImportFolderRecursivelly() throws Exception {                                        
        File folder = createFolder("folder");
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder1, "file");
                
        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        
        ISVNClientAdapter c = getNbClient();
        SVNUrl url = getTestUrl().appendPath(getName());
        c.mkdir(url, "mrkvadir");        
        url = url.appendPath(folder.getName());
        c.doImport(folder, url, "imprd", true);

        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        assertStatus(SVNStatusKind.UNVERSIONED, folder1);
        assertStatus(SVNStatusKind.UNVERSIONED, file);
        
        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), info.getUrl().toString());
        
        url = url.appendPath(folder1.getName());
        info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), info.getUrl().toString());
        
        url = url.appendPath(file.getName());
        info = getInfo(url);        
        assertNotNull(info);        
        assertEquals(url.toString(), info.getUrl().toString());
    }
    
}
