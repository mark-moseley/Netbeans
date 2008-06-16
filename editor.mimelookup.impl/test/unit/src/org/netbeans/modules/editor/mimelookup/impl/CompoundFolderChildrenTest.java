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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 *
 * @author vita
 */
public class CompoundFolderChildrenTest extends NbTestCase {

    /** Creates a new instance of FolderChildrenTest */
    public CompoundFolderChildrenTest(String name) {
        super(name);
    }

    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new String[] {
                "Tmp/"
            },
            getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            null
        );
        Logger.getLogger("org.openide.filesystems.Ordering").setLevel(Level.OFF);
    }

    // test collecting files on different layers

    public void testCollecting() throws Exception {
        String fileName1 = "file-on-layer-1.instance";
        String fileName2 = "file-on-layer-2.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);

        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 2, files.size());
        assertNotNull("Files do not contain " + fileName1, findFileByName(files, fileName1));
        assertNotNull("Files do not contain " + fileName2, findFileByName(files, fileName2));
        
        cfch = new CompoundFolderChildren(new String [] { "Tmp/X/Y/Z" });
        files = cfch.getChildren();

        assertEquals("Wrong number of files", 0, files.size());
    }
    
    // test hiding files on lower layer by files on higher layers

    public void testHidingSameFilesOnLowerLayers() throws Exception {
        String fileName = "some-file.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName);

        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong layerA file", fileName, ((FileObject) files.get(0)).getNameExt());
    }
    
    // test hidden files

// This one's failing, because the filesystem doesn't show files with the _hidden suffix
//    public void testFilesHiddenBySuffix() throws Exception {
//        String fileName1 = "file-on-layer-A.instance";
//        String fileName2 = "file-on-layer-B.instance";
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/" + fileName2);
//
//        File markerFile = new File(getWorkDir(), "Tmp/A/B/C/D/" + fileName2 + "_hidden");
//        markerFile.createNewFile();
//        
//        // Check precondition
//        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D/");
//        f.refresh();
//        
//        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D/" + fileName2 + "_hidden");
//        assertNotNull("The _hidden file does not exist", f);
//
//        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/" + fileName2);
//        assertNotNull("The original file on the second layer that should be hidden does not exist", f);
//
//        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/" + fileName2);
//        assertNotNull("The original file on the third layer that should be hidden does not exist", f);
//        
//        // Test compound children
//        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
//        List files = cfch.getChildren();
//        
//        assertEquals("Wrong number of files", 1, files.size());
//        assertEquals("Wrong layerA file", fileName1, ((FileObject) files.get(0)).getNameExt());
//    }

    public void testFilesHiddenByAttribute() throws Exception {
        String fileName1 = "file-on-layer-A.instance";
        String fileName2 = "file-on-layer-B.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName2);

        // Check precondition
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/" + fileName2);
        assertNotNull("The hidden file on the second layer does not exist", f);

        // Mark the file as hidden, which should hide both this file and
        // the same one on the third layer.
        f.setAttribute("hidden", Boolean.TRUE);
        
        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/" + fileName2);
        assertNotNull("The original file on the third layer that should be hidden does not exist", f);
        
        // Test compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong layerA file", fileName1, ((FileObject) files.get(0)).getNameExt());
    }
    
    // test sorting using attributes on different layers

    public void testSorting() throws Exception {
        // Create files
        String fileName1 = "file-1.instance";
        String fileName2 = "file-2.instance";
        String fileName3 = "file-3.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Set the sorting attributes
        FileObject layer1 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D");
        FileObject layer2 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B");
        FileObject layer3 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A");
        
        layer1.setAttribute("file-3.instance/file-1.instance", Boolean.TRUE);
        layer2.setAttribute("file-2.instance/file-3.instance", Boolean.TRUE);
        
        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName2, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName3, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName1, ((FileObject) files.get(2)).getNameExt());
    }

    public void testSorting2() throws Exception {
        // Create files
        String fileName1 = "Zfile.instance";
        String fileName2 = "Yfile.instance";
        String fileName3 = "Xfile.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName1, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName2, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName3, ((FileObject) files.get(2)).getNameExt());
    }
    
    // According to the weak stability clause in Utilities.topologicalSort this
    // test could be failing. But it is the behavior we would like to have. The
    // test seems to be passing, but probably just by sheer luck. In general U.tS
    // could move file-1.instance anywhere it likes.
    public void testSorting3() throws Exception {
        // Create files
        String fileName1 = "file-1.instance";
        String fileName2 = "file-2.instance";
        String fileName3 = "file-3.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Set the sorting attributes
        FileObject layer1 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D");
        FileObject layer2 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B");
        FileObject layer3 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A");
        
        layer2.setAttribute("file-3.instance/file-2.instance", Boolean.TRUE);
        
        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName1, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName3, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName2, ((FileObject) files.get(2)).getNameExt());
    }

    public void testSortingPositional() throws Exception {
        // Create files
        String fileName1 = "file-1.instance";
        String fileName2 = "file-2.instance";
        String fileName3 = "file-3.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Set the sorting attributes
        FileObject layer1 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D");
        FileObject layer2 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B");
        FileObject layer3 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A");

        layer1.getFileObject(fileName1).setAttribute("position", 300);
        layer2.getFileObject(fileName2).setAttribute("position", 100);
        layer3.getFileObject(fileName3).setAttribute("position", 200);
        
        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName2, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName3, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName1, ((FileObject) files.get(2)).getNameExt());
    }

    // test events

    private FileObject findFileByName(List files, String nameExt) {
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            FileObject f = (FileObject) i.next();
            if (nameExt.equals(f.getNameExt())) {
                return f;
            }
        }
        return null;
    }
    
    private static class L implements PropertyChangeListener {
        public int changeEventsCnt = 0;
        public PropertyChangeEvent lastEvent = null;
        
        public void propertyChange(PropertyChangeEvent evt) {
            changeEventsCnt++;
            lastEvent = evt;
        }
        
        public void reset() {
            changeEventsCnt = 0;
            lastEvent = null;
        }
    } // End of L class

    /* TBD whether any of the following, originally from FolderChildrenTest, are still applicable:
    public void testSimple() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);

        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        List files = fch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());
        
        fch = new FolderChildren("Tmp/X/Y/Z");
        files = fch.getChildren();

        assertEquals("Wrong number of files", 0, files.size());
    }

    public void testAddingFolders() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        List files = fch.getChildren();

        assertEquals("Wrong number of files", 0, files.size());
        
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);
        
        files = fch.getChildren();
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());
    }

    public void testRemovingFolders() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);

        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        List files = fch.getChildren();

        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());
        
        TestUtilities.deleteFile(getWorkDir(), "Tmp/A/");
        
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }
    
    public void testMultipleAddRemove() throws Exception {
        for (int i = 0; i < 7; i++) {
            testAddingFolders();
            testRemovingFolders();
        }
    }
    
    public void testChangeEvents() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        L listener = new L();
        fch.addPropertyChangeListener(listener);
        
        List files = fch.getChildren();
        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        assertEquals("Wrong number of files", 0, files.size());
        
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);

        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_CHILDREN, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());

        listener.reset();
        
        TestUtilities.deleteFile(getWorkDir(), "Tmp/A/");

        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_CHILDREN, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }

    public void testEventsWithMultipleChanges() throws Exception {
        for (int i = 0; i < 11; i++) {
            testChangeEvents();
        }
    }

    public void testEmptyFolder() throws Exception {
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        L listener = new L();
        fch.addPropertyChangeListener(listener);
        
        List files = fch.getChildren();
        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        assertEquals("Wrong number of files", 0, files.size());
        
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/");

        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());

        listener.reset();
        
        TestUtilities.deleteFile(getWorkDir(), "Tmp/A/");

        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }

    public void testAttributeChanges() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/");
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        L listener = new L();
        fch.addPropertyChangeListener(listener);
        
        List files = fch.getChildren();
        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        assertEquals("Wrong number of files", 0, files.size());
        
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D");
        assertNotNull("Can't find the folder", f);
        
        f.setAttribute("attrName", "attrValue");
        
        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_ATTRIBUTES, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());

        listener.reset();
        
        f.setAttribute("attrName", null);

        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_ATTRIBUTES, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }
     */

}
