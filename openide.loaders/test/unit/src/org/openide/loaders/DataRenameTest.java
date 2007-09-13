/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.openide.loaders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

/** Test things about node delegates.
 * Note: if you mess with file status changes in this test, you may effectively
 * break the testLeakAfterStatusChange test.
 *
 * @author Jesse Glick
 */
public class DataRenameTest extends NbTestCase {
    Logger LOG;
    
    public DataRenameTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }
    
    

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        
        MockServices.setServices(Pool.class);
    }
    public void testRenameBehaviour() throws Exception {
        File fJava = new File(getWorkDir(), "F.java");
        fJava.createNewFile();
        File dir = new File(getWorkDir(), "dir");
        dir.mkdirs();

        //LocalFileSystem lfs = new LocalFileSystem();
        //lfs.setRootDirectory(getWorkDir());
        FileObject root = FileUtil.toFileObject(getWorkDir());
        //FileObject root = lfs.getRoot();
        assertNotNull("root found", root);
        
        DataObject my = DataObject.find(root.getFileObject("F.java"));
        assertEquals(WithRenameObject.class, my.getClass());
        
        DataFolder f = DataFolder.findFolder(root.getFileObject("dir"));
        
        DataObject res = my.createFromTemplate(f);

        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("F.java", all[0]);
        }
        
        res.rename("Jarda");

        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("Jarda.java", all[0]);
        }
    }

    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders () {
            return org.openide.util.Enumerations.<DataLoader>singleton(MyLoader.getLoader(MyLoader.class));
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
        public MyLoader() {
            super(WithRenameObject.class.getName ());
            getExtensions().addExtension("java");
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new WithRenameObject(this, primaryFile);
        }
    }
    public static final class WithRenameObject extends MultiDataObject {
        public WithRenameObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }

        @Override
        protected FileObject handleRename(String name) throws IOException {
            return super.handleRename(name);
        }
        
    }
    
}
