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

package org.openide.loaders;

import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Test that URL is not requested if there are no broken shadows.
 * @author Jaroslav Tulach
 */
public class DataShadowBrokenAreNotTestedTest extends NbTestCase {
    
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    public DataShadowBrokenAreNotTestedTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        
        FileSystem lfs = Repository.getDefault().getDefaultFileSystem();
        
        FileObject[] delete = lfs.getRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }
        
        UM.cnt = 0;
    }
    
    public void testNoURLMapperQueried() throws Exception {
        FileSystem lfs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = FileUtil.createData(lfs.getRoot(), getName() + "/folder/original.txt");
        assertNotNull(fo);
        
        assertEquals("No queries to UM yet", 0, UM.cnt);
        DataObject original = DataObject.find(fo);
        
        assertEquals("No queries to UM after creation of data object", 0, UM.cnt);
    }
    
    public void testQueriedWhenBrokenShadowsExists() throws Exception {
        
        //
        // Note: if anyone lowers the number of queries done here,
        // then go on, this test is here just to describe the current behaviour
        //
        
        
        FileSystem lfs = Repository.getDefault().getDefaultFileSystem();
        FileObject f1 = FileUtil.createData(lfs.getRoot(), getName() + "/folder/original.txt");
        assertNotNull(f1);
        FileObject f2 = FileUtil.createData(lfs.getRoot(), getName() + "/any/folder/original.txt");
        assertNotNull(f2);
        
        assertEquals("No queries to UM yet", 0, UM.cnt);
        DataObject original = DataObject.find(f1);
        assertEquals("No queries to UM still", 0, UM.cnt);
        DataShadow s = original.createShadow(original.getFolder());
        assertEquals("One query to create the shadow and one to create the instance", 2, UM.cnt);
        original.delete();
        assertEquals("One additional query to delete", 3, UM.cnt);
        DataObject brokenShadow = DataObject.find(s.getPrimaryFile());
        assertEquals("Creating one broken shadow", 5, UM.cnt);
        
        DataObject original2 = DataObject.find(f2);
        assertEquals("Additional query per very data object creation", 6, UM.cnt);
    }
    
    private static final class UM extends URLMapper {
        public static int cnt;
        
        public URL getURL(FileObject fo, int type) {
            cnt++;
            return null;
        }
        
        public FileObject[] getFileObjects(URL url) {
            cnt++;
            return null;
        }
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[] {
                Lookups.singleton(new UM()),
                Lookups.metaInfServices(Lkp.class.getClassLoader()),
            });
        }
    }
    
}
