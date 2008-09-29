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

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/** Test things about shadows and broken shadows, etc.
 * @author Jaroslav Tulach
 */
public class DataShadowTest extends NbTestCase
implements java.net.URLStreamHandlerFactory {
    /** original object */
    private DataObject original;
    /** folder to work with */
    private DataFolder folder;
    /** fs we work on */
    private FileSystem lfs;

    private Logger err;
    
    static {
        // to handle nbfs urls...
        java.net.URL.setURLStreamHandlerFactory (new DataShadowTest (null));
    }
    
    public DataShadowTest (String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.FINER;
    }
    
    protected void setUp() throws Exception {
        
        lfs = Repository.getDefault ().getDefaultFileSystem ();
        
        FileObject[] delete = lfs.getRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }
        
        FileObject fo = FileUtil.createData (lfs.getRoot (), getName () + "/folder/original.txt");
        assertNotNull(fo);
        original = DataObject.find (fo);
        assertFalse ("Just to be sure that this is not shadow", original instanceof DataShadow);
        assertFalse ("And is some kind of subclass of DO", original.getClass () == DataObject.class);
        fo = FileUtil.createFolder (lfs.getRoot (), getName () + "/modify");
        assertNotNull(fo);
        assertTrue (fo.isFolder ());
        folder = DataFolder.findFolder (fo);
        
        Repository.getDefault ().addFileSystem (lfs);
        
        err = Logger.getLogger(getName());
    }
    
    public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals ("nbfs")) {
            return FileUtil.nbfsURLStreamHandler ();
        }
        return null;
    }
    
    public void testCreateTheShadow61175() throws Exception {
        if (!Utilities.isUnix()) {
            return;
        }
        
        final LocalFileSystem lfs = new LocalFileSystem() {
            public Status getStatus() {
                return new TestStatus(this);
            }
            
            class TestStatus implements FileSystem.Status {
                FileSystem lfs;
                TestStatus(FileSystem lfs) {
                    this.lfs = lfs;
                }
                public String annotateName(String name, java.util.Set files) {
                    if (files.size() > 0) {
                        try {
                            FileSystem fs = ((FileObject)files.toArray()[0]).getFileSystem();
                            assertEquals(fs, lfs);
                        } catch(FileStateInvalidException fsx) {}
                        
                    }
                    return name;
                }
                
                public java.awt.Image annotateIcon(java.awt.Image icon, int iconType, java.util.Set files) {
                    return icon;
                }
            }
        };
        
        
        lfs.setRootDirectory(new File("/"));
        Repository.getDefault().addFileSystem(lfs);
        
        
        DataFolder what  = DataFolder.findFolder(lfs.getRoot());
        assertNotNull(what);
        FileObject whereFo = Repository.getDefault().getDefaultFileSystem().getRoot();
        assertNotNull(whereFo);
        
        DataFolder where = DataFolder.findFolder(whereFo);
        assertNotNull(where);
        
        DataShadow shade = what.createShadow(where);
        assertNotNull(shade);
        
        Node node= shade.getNodeDelegate();
        assertNotNull(node);
        node.getIcon (java.beans.BeanInfo.ICON_COLOR_16x16);
        
    }
    
    public void testBrokenShadow55115 () throws Exception {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject brokenShadow = FileUtil.createData(sfs.getRoot(),"brokenshadows/brokon.shadow");
        assertNotNull (brokenShadow);
        // intentionally not set attribute "originalFile" to let that shadow be broken 
        //brokenShadow.setAttribute("originalFile", null);
        BrokenDataShadow bds = (BrokenDataShadow)DataObject.find(brokenShadow);
        assertNotNull (bds);
        URL url = bds.getUrl();
        //this call proves #55115 - but just in case if there is reachable masterfs 
        // - probably unwanted here
        bds.refresh(); 
        
        //If masterfs isn't reachable - second test crucial for URL,File, FileObject conversions
        // not necessary to be able to convert - but at least no IllegalArgumentException is expected
        if ("file".equals(url.getProtocol())) {
            new File (URI.create(url.toExternalForm()));
        }
    }
    
    public void testCreateTheShadow () throws Exception {
        DataShadow shade = original.createShadow (folder);
        
        assertEquals ("Shadow's original is the one", original, shade.getOriginal ());
        
        Object cookie = shade.getCookie (DataObject.class);
        assertEquals ("The shadow is own data object", shade, cookie);
        
        cookie = shade.getCookie (original.getClass ());
        assertEquals ("But it also returns the original when requested", original, cookie);
        
        URL u = DataShadow.readURL(shade.getPrimaryFile());
        assertEquals("DataShadow's URL must point to the Original", original.getPrimaryFile().getURL(), u);
    }

    @RandomlyFails // NB-Core-Build #1428
    public void testDeleteInvalidatesCreateCreates () throws Exception {
        doDeleteInvalidatesCreateCreates (true);
    }
    
    /* This is not implemented and could cause problems when module is enabled
     * and there is a link to a file in its layer - this link could possibly
     * not be updated (until creation of another data object)
     */
    @RandomlyFails // NB-Core-Build #1441
    public void testDeleteInvalidatesCreateCreatesJustOnFileSystemLevel () throws Exception {
        doDeleteInvalidatesCreateCreates (false);
    }

    private void doDeleteInvalidatesCreateCreates (boolean createDataObjectOrNot) throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();

        assertTrue ("Is valid now", shade.isValid ());
        err.info("Before delete");
        original.delete ();
        err.info("After delete");
        
        assertFalse ("Shadow is not valid anymore", shade.isValid ());
        assertFalse ("Original is gone", original.isValid ());
        
        err.info("Going to find new shadow");
        DataObject shade2 = DataObject.find (primary);
        err.info("Found: " + shade2);
        assertEquals ("Represents broken shadow (a bit implemetnation detail, but useful test)", BrokenDataShadow.class, shade2.getClass ());
        assertFalse ("Is not data shadow", shade2 instanceof DataShadow);
        
        // recreates the original
        err.info("Before recreation of the original");
        FileObject original2 = FileUtil.createData (lfs.getRoot (), original.getPrimaryFile ().getPath ());
        err.info("Original is there: " + original2);
        DataObject obj2;
        
        if (createDataObjectOrNot) {
            err.info("Now get the data object");
            obj2 = DataObject.find (original2);
            err.info("Object is there: " + obj2);
        }
        
        assertFalse ("Previous is not valid anymore", shade2.isValid ());
        
        DataObject shade3 = DataObject.find (primary);
        err.info("Shade3 is here: " + shade3);
        assertTrue ("it is a data shadow again", shade3 instanceof DataShadow);
        assertEquals ("Points to the same filename", original.getPrimaryFile ().getPath (), ((DataShadow)shade3).getOriginal ().getPrimaryFile ().getPath ());

        err.info("Before find for " + original2);
        assertEquals ("But of course the original is newly created", DataObject.find (original2), ((DataShadow)shade3).getOriginal ());

        err.info("Before shade.getOriginal()");
        assertEquals ("The old shadow is updated & getOriginal() waits for that to happen", original2, shade.getOriginal().getPrimaryFile());
    }

    public void testDeleteInvalidatesCreateCreatesWhenChangeHappensInAtomicAction () throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();

        assertTrue ("Is valid now", shade.isValid ());
        
        class DeleteCreate implements FileSystem.AtomicAction {
            public FileObject fo;
            
            public void run () throws java.io.IOException {
                FileSystem fs = original.getPrimaryFile ().getFileSystem ();
                String create = original.getPrimaryFile ().getPath ();
                original.getPrimaryFile ().delete ();
                
                fo = FileUtil.createData (fs.getRoot (), create);
            }
        }
        DeleteCreate deleteCreate = new DeleteCreate ();
        original.getPrimaryFile ().getFileSystem ().runAtomicAction (deleteCreate);
        
        assertTrue ("Shadow is valid (again)", shade.isValid ());
        assertFalse ("Original is gone", original.isValid ());
        DataObject orig = DataObject.find (deleteCreate.fo);
        if (orig == original) {
            fail ("new original shall be created");
        }
        assertTrue ("New orig is valid", orig.isValid ());
        
        // life would be nicer without this sleep, but somewhere inside
        // the DataShadow validation a request is send to RP with a delay
        // to not slow down regular apps. If you managed to kill next line,
        // you will have done the right job. Meanwhile it is here:
        Thread.sleep (2000);
        
        assertEquals ("Shadow's original is updated", orig, shade.getOriginal ());
    }
    
    public void testRenameUpdatesTheShadowIfItExists () throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();
        
        original.rename ("newname.txt");
        
        WeakReference<Object> ref = new WeakReference<Object>(shade);
        shade = null;
        assertGC ("Shadow can disappear", ref);
        
        DataObject obj = DataObject.find (primary);
        assertEquals ("It is shadow", DataShadow.class, obj.getClass ());
        shade = (DataShadow)obj;
        
        assertEquals ("And points to original with updated name", original, shade.getOriginal ());
        
        assertEquals("Shadow is own data object2", shade, shade.getCookie(DataObject.class));
        assertEquals("Shadow is own data object", shade, shade.getLookup().lookup(DataObject.class));
        assertEquals("Shadow is has the other object2", original, shade.getCookie(original.getClass()));
        assertEquals("Shadow is has the other object", original, shade.getLookup().lookup(original.getClass()));
        
    }
    
    public void testRenameDoesNotUpdateTheShadowIfItDoesNotExist () throws Exception {
        //
        // Not sure if this is the desired behaviour, however it is the
        // one currently implemented
        //
        
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();
        
        WeakReference ref = new WeakReference (shade);
        shade = null;
        assertGC ("Shadow can disappear", ref);
        
        original.rename ("newname");
        
        
        DataObject obj = DataObject.find (primary);
        assertEquals ("It is broken shadow", BrokenDataShadow.class, obj.getClass ());
    }
    
    public void testBrokenShadowNodeProperties() throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();
        
        assertTrue ("Is valid now", shade.isValid ());
        original.delete ();

        DataObject obj = DataObject.find (primary);
        assertEquals ("Instance class must be BrokenDataShadow", BrokenDataShadow.class, obj.getClass ());
        
        Node node = obj.getNodeDelegate ();
        
        Node.Property link = findProperty (node, "BrokenLink");
        assertNotNull ("Link must be non null string", (String)link.getValue ());
        
        assertTrue ("Is writeable", link.canWrite ());
        // this will revalidate the link
        FileObject fo = FileUtil.createData (lfs.getRoot (), getName () + "/folder/orig.txt");
        link.setValue (fo.getURL().toExternalForm());
       
        assertFalse ("The change of link should turn the shadow to valid one and invalidate this broken shadow", obj.isValid ());
        
        DataObject newObj = DataObject.find (primary);
        assertEquals ("This is a shadow", DataShadow.class, newObj.getClass ());
        shade = (DataShadow)newObj;
        
        assertEquals ("Points to the new file", getName () + "/folder/orig.txt", shade.getOriginal ().getPrimaryFile ().getPath ());
    }
    
    public void testFindOriginalFromAnonymousFilesystem() throws Exception {
        // Helpful for XML layer editing.
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject orig = FileUtil.createData(fs.getRoot(), "path/to/orig");
        FileObject shadow = FileUtil.createData(fs.getRoot(), "link.shadow");
        shadow.setAttribute("originalFile", "path/to/orig");
        assertEquals("found the right original file", DataObject.find(orig), DataShadow.deserialize(shadow));
        orig = FileUtil.createData(fs.getRoot(), "path to orig");
        shadow = FileUtil.createData(fs.getRoot(), "link2.shadow");
        shadow.setAttribute("originalFile", "path to orig");
        assertEquals("found the right original file", DataObject.find(orig), DataShadow.deserialize(shadow));
    }
    
    private static Node.Property findProperty (Node n, String name) {
        Node.PropertySet[] arr = n.getPropertySets ();
        StringBuffer names = new StringBuffer ();
        
        String prefix = "";
        for (int i = 0; i < arr.length; i++) {
            Node.PropertySet set = arr[i];
            Node.Property[] properties = set.getProperties ();
            for (int j = 0; j < properties.length; j++) {
                Node.Property p = properties[j];
                if (name.equals (p.getName ())) {
                    return p;
                }
                names.append (prefix);
                names.append (p.getName ());
                prefix = ", ";
            }
        }
        
        fail ("Cannot find property \"" + name + "\" in node " + n + " it has only " + names + " propeties.");
        return null;
    }
}
