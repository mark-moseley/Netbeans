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


import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import java.beans.PropertyChangeListener;
import junit.framework.Test;

/** Check what can be done when registering loaders in layer.
 * @author Jaroslav Tulach
 */
public class DataLoaderInLayerTest extends NbTestCase {

    public DataLoaderInLayerTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return new NbTestSuite(DataLoaderInLayerTest.class);
        //return new DataLoaderInLayerTest("testSimpleLoader");
    }
    
    protected FileSystem createFS(String... resources) throws IOException {
        return TestUtilHid.createLocalFileSystem(getWorkDir(), resources);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        FileUtil.setMIMEType("simple", "text/plain");
        FileUtil.setMIMEType("ant", "text/ant+xml");
    }
    
    private static void addRemoveLoader(DataLoader l, boolean add) throws IOException {
        addRemoveLoader("text/plain", l, add);
    }
    private static void addRemoveLoader(String mime, DataLoader l, boolean add) throws IOException {
        String res = "Loaders/" + mime + "/Factories/" + l.getClass().getSimpleName().replace('.', '-') + ".instance";
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        if (add) {
            FileObject fo = FileUtil.createData(root, res);
            fo.setAttribute("instanceCreate", l);
        } else {
            FileObject fo = root.getFileObject(res);
            if (fo != null) {
                fo.delete();
            }
        }
    }
    
    public void testSimpleGetChildren() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testSimpleLoader() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(SimpleDataObject.class, dob.getClass());
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testDataObjectFind() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            
            DataObject jdo = DataObject.find(fo);
            for (int i = 0; i < 5000; i++) {
                FileObject primary = jdo.getPrimaryFile();
                jdo.setValid(false);
                jdo = DataObject.find(primary);
                assertNotNull(jdo);
                assertTrue(jdo.isValid());
            }
            
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testAntAsAntSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("text/xml", l3, true);
        try {
            FileSystem lfs = createFS(new String[] {
                "folder/file.ant",
            });
            FileObject fo = lfs.findResource("folder/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(l2, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("text/xml", l3, false);
        }
    }
    public void testAntWithoutAntSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        //DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        //addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("text/xml", l3, true);
        try {
            FileSystem lfs = createFS("folder2/file.ant");
            FileObject fo = lfs.findResource("folder2/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            MultiFileLoader xmlL = DataLoader.getLoader(XMLDataObject.Loader.class);
            assertEquals("No special handling for XML", xmlL, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        //addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("text/xml", l3, false);
        }
    }

    public void testAntAsUnknownSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        //DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        //addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("content/unknown", l3, true);
        try {
            FileSystem lfs = createFS("folder3/file.ant");
            FileObject fo = lfs.findResource("folder3/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(l3, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        //addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("content/unknown", l3, false);
        }
    }
    
    public void testManifestRegistrationsTakePreceedence() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        addRemoveLoader("text/ant+xml", l2, true);
        AddLoaderManuallyHid.addRemoveLoader(l3, true);
        try {
            FileSystem lfs = createFS("folder4/file.ant");
            FileObject fo = lfs.findResource("folder4/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals("Old registration of l3 takes preceedence", l3, dob.getLoader());
        } finally {
            addRemoveLoader(l1, false);
            addRemoveLoader("text/ant+xml", l2, false);
            AddLoaderManuallyHid.addRemoveLoader(l3, false);
        }
    }
    
    public static final class XMLUniFileLoader extends SimpleUniFileLoader {
        @Override
        protected void initialize() {
            getExtensions().addMimeType("text/xml");
            getExtensions().addMimeType("text/ant+xml");
        }
    }
    public static final class AntUniFileLoader extends SimpleUniFileLoader {
        @Override
        protected void initialize() {
            getExtensions().addMimeType("text/xml");
            getExtensions().addMimeType("text/ant+xml");
        }
    }
    public static class SimpleUniFileLoader extends UniFileLoader {
        public SimpleUniFileLoader() {
            super(SimpleDataObject.class.getName());
        }
        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("simple");
        }
        protected String displayName() {
            return "Simple";
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new SimpleDataObject(pf, this);
        }
    }
    public static final class SimpleDataObject extends MultiDataObject {
        private ArrayList supp = new ArrayList ();
        
        public SimpleDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
            super(pf, loader);
        }
        
        /** Access method to modify cookies 
         * @return cookie set of this data object
         */
        public final org.openide.nodes.CookieSet cookieSet () {
            return getCookieSet ();
        }
        
        /** Getter for list of listeners attached to the data object.
         */
        public final Enumeration listeners () {
            return Collections.enumeration (supp);
        }
        
        @Override
        public void addPropertyChangeListener (PropertyChangeListener l) {
            super.addPropertyChangeListener (l);
            supp.add (l);
        }

        @Override
        public void removePropertyChangeListener (PropertyChangeListener l) {
            super.removePropertyChangeListener (l);
            supp.remove (l);
        }        
    }
    
}
