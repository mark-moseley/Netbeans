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

package org.netbeans.modules.masterfs.filebasedfs;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtilTestHidden;
import org.openide.filesystems.URLMapperTestHidden;

/**
 * @author rmatous
 */
public class FileBasedFileSystemTest extends FileSystemFactoryHid {            
    public FileBasedFileSystemTest(Test test) {
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
        return new FileBasedFileSystemTest(suite);
    }
        
    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        return new File(workDirProperty);
    }
            
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        FileBasedFileSystem.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(workFo);
        for (int i = 0; i < resources.length; i++) {
            String res = resources[i];
            if (res.endsWith("/")) {
                assertNotNull(FileUtil.createFolder(workFo,res));
            } else {
                assertNotNull(FileUtil.createData(workFo,res));
            }
        }
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    protected void destroyFileSystem(String testName) throws IOException {}    

    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir()).getPath();
    }
}
