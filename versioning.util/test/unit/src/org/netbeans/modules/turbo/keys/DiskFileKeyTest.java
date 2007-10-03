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
package org.netbeans.modules.turbo.keys;

import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileSystem;
import org.netbeans.modules.turbo.Turbo;

import java.io.File;

/**
 * Test DiskFileKey
 *
 * @author Petr Kuzel
 */
public class DiskFileKeyTest extends TestCase {

    private FileSystem fs;

    // called before every method
    protected void setUp() throws Exception {

        // prepare simple LFS

        LocalFileSystem fs = new LocalFileSystem();
        File tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + "turbo-test");
        tmp.mkdir();
        tmp.deleteOnExit();
        File theFile = new File(tmp, "theFile");
        theFile.createNewFile();
        theFile.deleteOnExit();
        fs.setRootDirectory(tmp);

        this.fs = fs;
    }


    /** Test how keys handle FileObject identity problems. */
    public void testMemoryKeys() throws Exception{
        FileObject overlap = fs.getRoot().createFolder("nestedFS2");
        try {
            LocalFileSystem ofs = new LocalFileSystem();
            ofs.setRootDirectory(FileUtil.toFile(overlap));

            ofs.getRoot().createData("data.txt");

            FileObject f1 = fs.findResource("nestedFS2/data.txt");
            FileObject f2 = ofs.findResource("data.txt");

            DiskFileKey k1 = DiskFileKey.createKey(f1);
            DiskFileKey k2 = DiskFileKey.createKey(f2);

            assertTrue(k1.equals(k2));

        } finally {
            overlap.delete(overlap.lock());
        }
    }

    /**
     * Netbeans fileobjects have problems with identity.
     * Two fileobject representing same file are not guaranteed to be equivalent.
     * Known causes: MasterFS fileobject and fileobject from wrapped
     * filesystem (it can be spotted only by FS impl). Overlapping
     * LocalFilesystems.
     * <p>
     * It uncovered Memory.DiskFileKey.hashCode problem!
     */
    public void testDuplicatedFileObject() throws Exception {
        FileObject overlap = fs.getRoot().createFolder("nestedFS");
        try {
            LocalFileSystem ofs = new LocalFileSystem();
            ofs.setRootDirectory(FileUtil.toFile(overlap));

            ofs.getRoot().createData("data.txt");

            FileObject f1 = fs.findResource("nestedFS/data.txt");
            FileObject f2 = ofs.findResource("data.txt");
            assert f1 != f2;
            assert f1.equals(f2) == false;
            Object k1 = DiskFileKey.createKey(f1);
            Object k2 = DiskFileKey.createKey(f2);


            Turbo turbo = Turbo.getDefault();

            turbo.writeEntry(k1, "identity", "clash");
            Object v1 = turbo.readEntry(k1, "identity");
            Object v2 = turbo.readEntry(k2, "identity");

            assertTrue("clash".equals(v1));
            assertTrue("Unexpected value:" + v2, "clash".equals(v2));

            turbo.writeEntry(k2, "identity", "over!");

            v1 = turbo.readEntry(k1, "identity");
            v2 = turbo.readEntry(k2, "identity");

            assertTrue("over!".equals(v1));
            assertTrue("Unexpected value:" + v2, "over!".equals(v2));

        } finally {
            overlap.delete(overlap.lock());
        }
    }

}
