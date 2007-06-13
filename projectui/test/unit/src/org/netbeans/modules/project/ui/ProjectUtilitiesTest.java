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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Tests storing and reloading project's documents in case of open/close project.
 *
 * @author Jiri Rechtacek
 */
public class ProjectUtilitiesTest extends NbTestCase {
    
    private static final String NAVIGATOR_MODE = "navigator";
    
    DataObject do1_1_open, do1_2_open, do1_3_close, do1_4_close;
    DataObject do2_1_open;
    Project project1, project2;
    Set<DataObject> openFilesSet = new HashSet<DataObject>();
    TopComponent tc1_1, tc1_2, tc2_1, tc1_1_navigator;
    
    public ProjectUtilitiesTest (String testName) {
        super (testName);
    }
    
    protected boolean runInEQ () {
        return true;
    }

    protected void setUp () throws Exception {
        super.setUp ();
        MockServices.setServices(TestSupport.TestProjectFactory.class, ProjectUtilitiesDummyWindowManager.class);
                                
        clearWorkDir ();
        
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
    
        //Mode mode = WindowManager.getDefault ().createWorkspace ("TestHelper").createMode (CloneableEditorSupport.EDITOR_MODE, CloneableEditorSupport.EDITOR_MODE, null);
        
        FileObject p1 = TestSupport.createTestProject (workDir, "project1");
        FileObject f1_1 = p1.createData("f1_1.java");
        FileObject f1_2 = p1.createData("f1_2.java");
        FileObject f1_3 = p1.createData("f1_3.java");
        FileObject f1_4 = p1.createData("f1_4.java");
        do1_1_open = DataObject.find (f1_1);
        do1_2_open = DataObject.find (f1_2);
        do1_3_close = DataObject.find (f1_3);
        do1_4_close = DataObject.find (f1_4);
        openFilesSet.add (do1_1_open);
        openFilesSet.add (do1_2_open);

        project1 = ProjectManager.getDefault ().findProject (p1);
        ((TestSupport.TestProject) project1).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        FileObject p2 = TestSupport.createTestProject (workDir, "project2");
        FileObject f2_1 = p2.createData("f2_1.java");
        do2_1_open = DataObject.find (f2_1);

        project2 = ProjectManager.getDefault ().findProject (p2);
        ((TestSupport.TestProject) project2).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        //it will be necessary to dock the top components into the "editor" and "navigator" modes, so they need to be created:
        createMode(CloneableEditorSupport.EDITOR_MODE);
        createMode(NAVIGATOR_MODE);
        (tc1_1 = new SimpleTopComponent (do1_1_open, CloneableEditorSupport.EDITOR_MODE)).open ();
        (tc1_2 = new SimpleTopComponent (do1_2_open, CloneableEditorSupport.EDITOR_MODE)).open ();
        (tc2_1 = new SimpleTopComponent (do2_1_open, CloneableEditorSupport.EDITOR_MODE)).open ();
        (tc1_1_navigator = new SimpleTopComponent2 (do1_1_open, NAVIGATOR_MODE)).open ();
        
        ExitDialog.SAVE_ALL_UNCONDITIONALLY = true;
    }
    
    @SuppressWarnings("deprecation")
    private static void createMode(String name) {
        WindowManager.getDefault().getWorkspaces()[0].createMode(name, name, null);
    }

    public void testCloseAllDocuments () {
        closeProjectWithOpenedFiles ();
    }
    
    private void closeProjectWithOpenedFiles () {
        AuxiliaryConfiguration aux = project1.getLookup().lookup(AuxiliaryConfiguration.class);
        assertNotNull ("AuxiliaryConfiguration must be present if project's lookup", aux);

        Element openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS, false);
        if (openFilesEl != null) {
            assertEquals ("OpenFiles element is empty or null.", 0, openFilesEl.getChildNodes ().getLength ());
        }
        
        Project[] projects = new Project[] {project1};
        
        if (ProjectUtilities.closeAllDocuments(projects, false)) {
            OpenProjectList.getDefault().close(projects, false);
        }
        
        openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS, false);
        assertNotNull ("OPEN_FILES_ELEMENT found in the private configuration.", openFilesEl);
        
        NodeList list = openFilesEl.getElementsByTagName (ProjectUtilities.FILE_ELEMENT);
        
        assertNotNull ("FILE_ELEMENT must be present", list);
        assertTrue ("Same count of FILE_ELEMENTs and open files, elements count " + list.getLength (), openFilesSet.size () == list.getLength ());
        
        for (int i = 0; i < list.getLength (); i++) {
            String url = list.item (i).getChildNodes ().item (0).getNodeValue ();
            FileObject fo = null;
            try {
                fo = URLMapper.findFileObject (new URL (url));
                assertNotNull ("Found file for URL " + url, fo);
                DataObject dobj = DataObject.find (fo);
                assertTrue (dobj + " is present in the set of open files.", openFilesSet.contains (dobj));
                assertNotSame ("The closed file are not present.", do1_3_close, dobj);
                assertNotSame ("The open file of other project is not present.", do2_1_open, dobj);
            } catch (MalformedURLException mue) {
                fail ("MalformedURLException in " + url);
            } catch (DataObjectNotFoundException donfo) {
                fail ("DataObject must exist for " + fo);
            }
        }
        
    }
    
    private void modifyDO(DataObject toModify) throws BadLocationException, IOException {
        System.err.println("toModify = " + toModify );
        EditorCookie ec = (EditorCookie) toModify.getCookie(EditorCookie.class);
        
        ec.openDocument().insertString(0, "test", null);
    }
    
    public void testSavingModifiedNotOpenedFiles67526() throws BadLocationException, IOException {
        AuxiliaryConfiguration aux = project1.getLookup().lookup(AuxiliaryConfiguration.class);
        assertNotNull ("AuxiliaryConfiguration must be present if project's lookup", aux);
        
        Element openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS, false);
        if (openFilesEl != null) {
            assertEquals ("OpenFiles element is empty or null.", 0, openFilesEl.getChildNodes ().getLength ());
        }
        
        modifyDO(do1_4_close);
        
        Project[] projects = new Project[] {project1};
        
        if (ProjectUtilities.closeAllDocuments(projects, true)) {
            OpenProjectList.getDefault().close(projects, true);
        }
        
        assertFalse("the do1_4_close not modified", do1_4_close.isModified());
        
        openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS, false);
        assertNotNull ("OPEN_FILES_ELEMENT found in the private configuration.", openFilesEl);
        
        NodeList list = openFilesEl.getElementsByTagName (ProjectUtilities.FILE_ELEMENT);
        
        assertNotNull ("FILE_ELEMENT must be present", list);
        assertTrue ("Same count of FILE_ELEMENTs and open files, elements count " + list.getLength (), openFilesSet.size () == list.getLength ());
        
        for (int i = 0; i < list.getLength (); i++) {
            String url = list.item (i).getChildNodes ().item (0).getNodeValue ();
            FileObject fo = null;
            try {
                fo = URLMapper.findFileObject (new URL (url));
                assertNotNull ("Found file for URL " + url, fo);
                DataObject dobj = DataObject.find (fo);
                System.err.println("openFilesSet = " + openFilesSet );
                assertTrue (dobj + " is present in the set of open files.", openFilesSet.contains (dobj));
                assertNotSame ("The closed file are not present.", do1_3_close, dobj);
                assertNotSame ("The open file of other project is not present.", do2_1_open, dobj);
            } catch (MalformedURLException mue) {
                fail ("MalformedURLException in " + url);
            } catch (DataObjectNotFoundException donfo) {
                fail ("DataObject must exist for " + fo);
            }
        }
    }
    
    public void testCloseAndOpenProjectAndClosedWithoutOpenFiles () {
        closeProjectWithOpenedFiles ();

        OpenProjectList.getDefault ().open (project1, false);

        Iterator/*<TopComponent>*/ openTCs = WindowManager.getDefault ().getRegistry ().getOpened ().iterator ();
        while (openTCs.hasNext ()) {
            assertTrue ("TopComponent has been closed successfully.", ((TopComponent)openTCs.next ()).close ());
        }
        
        if (ProjectUtilities.closeAllDocuments(new Project[] {project1}, false)) {
            OpenProjectList.getDefault().close(new Project[] {project1}, false);
        }

        AuxiliaryConfiguration aux = project1.getLookup().lookup(AuxiliaryConfiguration.class);
        Element openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS, false);
        assertNull ("OPEN_FILES_ELEMENT not found in the private configuration.", openFilesEl);
        
        assertFalse ("Project1 must be closed.", OpenProjectList.getDefault ().isOpen (project1));
    }
    
    public void testCanUseFileName() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        FileObject p1 = d.getFileObject("project1");
        assertNotNull(p1);
        assertNull("normal file addition", ProjectUtilities.canUseFileName(p1, null, "foo", "java", false));
        assertNull("normal file addition with no extension is OK", ProjectUtilities.canUseFileName(p1, null, "foo", null, false));
        assertNull("normal file addition in an existing subdir", ProjectUtilities.canUseFileName(d, "project1", "foo", "java", false));
        assertNull("normal file addition in a new subdir", ProjectUtilities.canUseFileName(d, "dir", "foo", "java", false));
        //assertNotNull("no target name", ProjectUtilities.canUseFileName(d, "dir", null, "java"));
        assertNotNull("no target folder", ProjectUtilities.canUseFileName(null, "dir", "foo", "java", false));
        assertNotNull("file already exists", ProjectUtilities.canUseFileName(p1, null, "f1_1", "java", false));
        assertNotNull("file already exists in subdir", ProjectUtilities.canUseFileName(d, "project1", "f1_1", "java", false));
        assertNull("similar file already exists in subdir", ProjectUtilities.canUseFileName(d, "project1", "f1_1", "properties", false));
        assertNull("similar file already exists in subdir", ProjectUtilities.canUseFileName(d, "project1", "f1_1", null, false));
        d = new XMLFileSystem().getRoot();
        assertNotNull("FS is r/o", ProjectUtilities.canUseFileName(d, null, "foo", "java", false));
        // #59876: deal with non-disk-based filesystems sensibly
        d = FileUtil.createMemoryFileSystem().getRoot();
        d.createData("bar.java");
        FileUtil.createData(d, "sub/dir/foo.java");
        assertNull("can create file in non-disk FS", ProjectUtilities.canUseFileName(d, null, "foo", "java", false));
        assertNotNull("file already exists", ProjectUtilities.canUseFileName(d, null, "bar", "java", false));
        assertNotNull("file already exists in subsubdir", ProjectUtilities.canUseFileName(d, "sub/dir", "foo", "java", false));
        assertNull("can otherwise create file in subsubdir", ProjectUtilities.canUseFileName(d, "sub/dir", "bar", "java", false));
        //#66792: allow to create whole directory tree at once using Folder Template:
        assertNull("can create directory subtree", ProjectUtilities.canUseFileName(d, null, "a/b/c", null, true));
        //#59654: do not allow slash and backslash for common templates:
        assertNotNull("cannot create file with slashes", ProjectUtilities.canUseFileName(d, null, "a/b/c", "txt", false));
        assertNotNull("cannot create file with backslashes", ProjectUtilities.canUseFileName(d, null, "a\\b\\c", "txt", false));
    }
    
    public void testNavigatorIsNotClosed() throws Exception {
        closeProjectWithOpenedFiles ();
        
        assertFalse(tc1_1.isOpened());
        assertFalse(tc1_2.isOpened());
        assertTrue(tc1_1_navigator.isOpened());
    }

    private static class SimpleTopComponent extends CloneableTopComponent {
        private Object content;
        private String modeToDockInto;
        public SimpleTopComponent (Object obj, String modeToDockInto) {
            this.content = obj;
            this.modeToDockInto = modeToDockInto;
            setName (obj.toString ());
        }
        
        public Lookup getLookup () {
            return Lookups.singleton (content);
        }
        
        public void open() {
            super.open();
            WindowManager.getDefault().findMode(modeToDockInto).dockInto(this);
        }
    }
    
    private static class SimpleTopComponent2 extends TopComponent {
        private Object content;
        private String modeToDockInto;
        public SimpleTopComponent2 (Object obj, String modeToDockInto) {
            this.content = obj;
            this.modeToDockInto = modeToDockInto;
            setName (obj.toString ());
        }
        
        public Lookup getLookup () {
            return Lookups.singleton (content);
        }
        
        public void open() {
            super.open();
            WindowManager.getDefault().findMode(modeToDockInto).dockInto(this);
        }
    }
    
}
