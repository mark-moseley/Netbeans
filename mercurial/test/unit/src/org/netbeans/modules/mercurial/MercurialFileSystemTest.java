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

package org.netbeans.modules.mercurial;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.filebasedfs.BaseFileObjectTestHid;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtilTestHidden;
import org.openide.filesystems.URLMapperTestHidden;

/**
 * @author Tomas Stupka
 */
public class MercurialFileSystemTest extends FileSystemFactoryHid {
        
    public MercurialFileSystemTest(Test test) {
        super(test);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});                
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();        
        
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);        
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);                        
        suite.addTestSuite(BaseFileObjectTestHid.class);                                
       
        // XXX fails
//        suite.addTest(new FileUtilTestHidden("testIsParentOf"));                
//        suite.addTest(new BaseFileObjectTestHid("testRootToFileObject"));                                
        return new MercurialFileSystemTest(suite);
    }
    
    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        return new File(workDirProperty);
    }

    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        
        FileObject workFo = null;
        try {
            init();
            
            FileObjectFactory.reinitForTests();
            workFo = FileBasedFileSystem.getFileObject(getWorkDir());
            assertNotNull(workFo);
            List<File> files = new ArrayList<File>(resources.length);
            for (int i = 0; i < resources.length; i++) {                
                String res = resources[i];
                FileObject fo;
                if (res.endsWith("/")) {
                    fo = FileUtil.createFolder(workFo, res);
                    assertNotNull(fo);
                } else {
                    fo = FileUtil.createData(workFo, res);
                    assertNotNull(fo);
                }                
                files.add(FileUtil.toFile(fo));                
            }
            commit(files);
            
        } catch (HgException ex) {
            throw new IOException(ex.getMessage());
        } 
        
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    protected void destroyFileSystem(String testName) throws IOException {}    

    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir()).getPath();
    }

    private void commit(List<File> files) throws HgException {       
        
        List<File> filesToAdd = new ArrayList<File>();
        FileInformation status;
        for (File file : files) {

            status = HgCommand.getSingleStatus(getWorkDir(), file.getParentFile().getAbsolutePath(), file.getName());
            if(status.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {                   
                filesToAdd.add(file);

                File parent = file.getParentFile();
                while (!getWorkDir().equals(parent)) {
                    status = HgCommand.getSingleStatus(getWorkDir(), parent.getParentFile().getAbsolutePath(), parent.getName());
                    if(status.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                        filesToAdd.add(0, parent);
                        parent = parent.getParentFile();
                    } else {
                        break;
                    }
                }                                    
            }    
        }            
            
        HgCommand.doAdd(getWorkDir(), filesToAdd, null);
        List<File> filesToCommit = new ArrayList<File>();
        for (File file : files) {
            if(file.isFile()) {
                filesToCommit.add(file);
            }
        }
        
        HgCommand.doCommit(getWorkDir(), filesToCommit, "commit", null);
//        for (File file : filesToCommit) {
//            assertStatus(file);
//        }        
    }

    private void assertStatus(File f) throws HgException {
        FileInformation status;
        status = HgCommand.getSingleStatus(getWorkDir(), f.getParentFile().getAbsolutePath(), f.getName());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, status.getStatus());
    }    
    
    private void init() throws HgException {
        HgCommand.doCreate(getWorkDir(), null);
    }    

}
