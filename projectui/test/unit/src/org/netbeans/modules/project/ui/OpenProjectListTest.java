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

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Tests fix of issue 56454.
 *
 * @author Jiri Rechtacek
 */
public class OpenProjectListTest extends NbTestCase {
    FileObject f1_1_open, f1_2_open, f1_3_close;
    FileObject f2_1_open;

    Project project1, project2;
    TestOpenCloseProjectDocument handler = new OpenProjectListTest.TestOpenCloseProjectDocument ();

    public OpenProjectListTest (String testName) {
        super (testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp () throws Exception {
        super.setUp ();
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir ();
        
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL = handler;
        
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
    
        FileObject p1 = TestSupport.createTestProject (workDir, "project1");
        f1_1_open = p1.createData("f1_1.java");
        f1_2_open = p1.createData("f1_2.java");
        f1_3_close = p1.createData("f1_3.java");

        project1 = ProjectManager.getDefault ().findProject (p1);
        ((TestSupport.TestProject) project1).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        FileObject p2 = TestSupport.createTestProject (workDir, "project2");
        f2_1_open = p2.createData ("f2_1.java");

        // project2 depends on projects1
        project2 = ProjectManager.getDefault ().findProject (p2);
        ((TestSupport.TestProject) project2).setLookup(Lookups.fixed(TestSupport.createAuxiliaryConfiguration(), new MySubprojectProvider(project1)));
        
        // prepare set of open documents for both projects
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_1_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_2_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f2_1_open);
        
        // close both projects with own open files
        OpenProjectList.getDefault().close(new Project[] {project1, project2}, false);
    }
    
    protected void tearDown () {
        OpenProjectList.getDefault().close(new Project[] {project1, project2}, false);
    }

    public void testOpen () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault ().open (project1, true);        
        assertTrue ("Project1 is opened.", OpenProjectList.getDefault ().isOpen (project1));
        Pattern p = Pattern.compile("Opening.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log.toString());
        }
        
        assertTrue ("Document f1_1_open is loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertTrue ("Document f1_2_open is loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
    }
    
    public void testListenerOpenClose () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0); 
        ChangeListener list = new ChangeListener();
        OpenProjectList.getDefault().addPropertyChangeListener(list);
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault ().open (project1, true);
        Pattern p = Pattern.compile("Opening.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log.toString());
        }
        assertEquals(0, list.oldCount);
        assertEquals(1, list.newCount);
        OpenProjectList.getDefault ().open (project2, true);
        assertEquals(1, list.oldCount);
        assertEquals(2, list.newCount);
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        assertEquals(2, list.oldCount);
        assertEquals(1, list.newCount);
        OpenProjectList.getDefault().close(new Project[] {project2}, false);
        assertEquals(1, list.oldCount);
        assertEquals(0, list.newCount);
    }
    
    public void testClose () throws Exception {
        testOpen ();
        
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        Pattern p = Pattern.compile("Closing.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log);
        }
        assertFalse ("Document f1_1_open isn't loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertFalse ("Document f1_2_open isn't loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
        
        OpenProjectList.getDefault ().open (project1);
        OpenProjectList.getDefault ().open (project2);
        
        // close all project1's documents
        handler.openFiles.remove (f1_1_open.getURL ().toExternalForm ());
        handler.openFiles.remove (f1_2_open.getURL ().toExternalForm ());
        
        ProjectUtilities.closeAllDocuments(new Project[] {project1}, false);
        OpenProjectList.getDefault().close(new Project[] {project1}, false);

        OpenProjectList.getDefault ().open (project1);
        assertFalse ("Document f1_1_open isn't loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertFalse ("Document f1_2_open isn't loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertTrue ("Document f2_1_open is still loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
    }

    @RandomlyFails // NB-Core-Build #810
    public void testSerialize() throws Exception {
        testOpen();
        
        OpenProjectList.waitProjectsFullyOpen();
        Field f = OpenProjectList.class.getDeclaredField("INSTANCE");
        f.setAccessible(true);
        f.set(null, null);
        
        CharSequence whatIsLoggedWhenDeserializing = Log.enable("org.netbeans.ui", Level.FINE);
        
        Project[] arr = OpenProjectList.getDefault().getOpenProjects();
        OpenProjectList.waitProjectsFullyOpen();
        arr = OpenProjectList.getDefault().getOpenProjects();
        
        assertEquals("One", 1, arr.length);
        Pattern p = Pattern.compile("Initializing.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(whatIsLoggedWhenDeserializing);
        if (!m.find()) {
            fail("There should be TestProject\n" + whatIsLoggedWhenDeserializing);
        }
    }
    
    public void testOpenDependingProject () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);        
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault ().open (project2, true);        
        assertTrue ("Project1 is opened.", OpenProjectList.getDefault ().isOpen (project1));
        assertTrue ("Project2 is opened.", OpenProjectList.getDefault ().isOpen (project2));
        Pattern p = Pattern.compile("Opening.*2.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log);
        }
        
        assertTrue ("Document f1_1_open is loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertTrue ("Document f1_2_open is loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertTrue ("Document f2_1_open is loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
    }
    
    public void testCloseProjectWithoutOpenDocuments () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);        
        OpenProjectList.getDefault ().open (project2, false);        
        assertFalse ("Project1 isn't opened.", OpenProjectList.getDefault ().isOpen (project1));
        assertTrue ("Project2 is opened.", OpenProjectList.getDefault ().isOpen (project2));
        
        handler.openFiles.remove (f2_1_open.getURL ().toExternalForm ());
        
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
        
        ProjectUtilities.closeAllDocuments(new Project[] {project2}, false);
        OpenProjectList.getDefault().close(new Project[] {project2}, false);

        assertFalse ("Project2 is closed.", OpenProjectList.getDefault ().isOpen (project2));
    }
    
    public void testProjectOpenedClosed() throws Exception {
        ((TestSupport.TestProject) project1).setLookup(Lookups.fixed(new Object[] {
            new TestProjectOpenedHookImpl(),
            new TestProjectOpenedHookImpl(),
        }));
        
        TestProjectOpenedHookImpl.opened = 0;
        TestProjectOpenedHookImpl.closed = 0;
        
        OpenProjectList.getDefault().open(project1);
        
        assertEquals("both open hooks were called", 2, TestProjectOpenedHookImpl.opened);
        assertEquals("no close hook was called", 0, TestProjectOpenedHookImpl.closed);
        
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        
        assertEquals("both open hooks were called", 2, TestProjectOpenedHookImpl.opened);
        assertEquals("both close hooks were called", 2, TestProjectOpenedHookImpl.closed);
    }
    
    public void testNotifyDeleted() throws Exception {
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        
        FileObject p1 = workDir.createFolder("p1");
        FileObject p1TestProject = p1.createFolder("testproject");
        
        Project project1 = ProjectManager.getDefault().findProject(p1);
        
        assertNotNull("project1 is recognized", project1);
        
        OpenProjectList.getDefault().open(project1);
        
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        
        p1TestProject.delete();
        TestSupport.notifyDeleted(project1);
        
        assertNull("project1 is deleted", ProjectManager.getDefault().findProject(p1));
        
        assertFalse("project1 is not in recent projects list", OpenProjectList.getDefault().getRecentProjects().contains(project1));
        
        FileObject p2 = workDir.createFolder("p2");
        FileObject p2TestProject = p2.createFolder("testproject");
        
        Project project2 = ProjectManager.getDefault().findProject(p2);
        
        assertNotNull("project2 is recognized", project2);
        OpenProjectList.getDefault().open(project2);
        
        OpenProjectList.getDefault().close(new Project[] {project2}, false);
        
        TestSupport.notifyDeleted(project2);
        
        assertFalse("project2 is not in recent projects list", OpenProjectList.getDefault().getRecentProjects().contains(project2));
    }
    
    public void testMainProject() throws Exception {
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        
        FileObject p1 = workDir.createFolder("p1");
        FileObject p1TestProject = p1.createFolder("testproject");
        
        Project project1 = ProjectManager.getDefault().findProject(p1);
        
        assertNotNull("project1 is recognized", project1);
        
        FileObject p2 = workDir.createFolder("p2");
        FileObject p2TestProject = p2.createFolder("testproject");
        
        Project project2 = ProjectManager.getDefault().findProject(p2);
        
        assertNotNull("project2 is recognized", project2);
        
        FileObject p3 = workDir.createFolder("p3");
        FileObject p3TestProject = p3.createFolder("testproject");
        
        Project project3 = ProjectManager.getDefault().findProject(p3);
        
        assertNotNull("project3 is recognized", project3);
        
        assertNull("no main project set when OPL is empty", OpenProjectList.getDefault().getMainProject());
        
        OpenProjectList.getDefault().open(project1);
        
        assertNull("open project does not change main project", OpenProjectList.getDefault().getMainProject());
        
        OpenProjectList.getDefault().setMainProject(project1);
        
        assertTrue("main project correctly set", OpenProjectList.getDefault().getMainProject() == project1);
        
        OpenProjectList.getDefault().open(project2);
        
        assertTrue("open project does not change main project", OpenProjectList.getDefault().getMainProject() == project1);
        
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        
        assertNull("no main project set when main project is closed", OpenProjectList.getDefault().getMainProject());
        
        boolean exceptionThrown = false;
        
        try {
            OpenProjectList.getDefault().setMainProject(project3);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        assertTrue("IAE thrown when trying to set main project that is not opened", exceptionThrown);
        
        //the same for a previously opened project:
        exceptionThrown = false;
        
        try {
            OpenProjectList.getDefault().setMainProject(project1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        assertTrue("IAE thrown when trying to set main project that is not opened", exceptionThrown);
    }
    // helper code

    private static class MySubprojectProvider implements SubprojectProvider {
        Project p;
        public MySubprojectProvider (final Project project) {
            p = project;
        }
        public Set<Project> getSubprojects() {
            return Collections.singleton (p);
        }
        
        public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {}
        public void addChangeListener (javax.swing.event.ChangeListener changeListener) {}

    }
    
    private static class TestOpenCloseProjectDocument implements ProjectUtilities.OpenCloseProjectDocument {
        public Set<String> openFiles = new HashSet<String>();
        public Map<Project,SortedSet<String>> urls4project = new HashMap<Project,SortedSet<String>>();
        
        public boolean open (FileObject fo) {
            Project owner = FileOwnerQuery.getOwner (fo);
            if (!urls4project.containsKey (owner)) {
              // add project
                urls4project.put(owner, new TreeSet<String>());
            }
            URL url = null;
            DataObject dobj = null;
            try {
                dobj = DataObject.find (fo);
                url = dobj.getPrimaryFile ().getURL ();
                urls4project.get(owner).add(url.toExternalForm());
                openFiles.add (fo.getURL ().toExternalForm ());
            } catch (FileStateInvalidException fsie) {
                fail ("FileStateInvalidException in " + dobj.getPrimaryFile ());
            } catch (DataObjectNotFoundException donfe) {
                fail ("DataObjectNotFoundException on " + fo);
            }
            return true;
        }
        
        public Map<Project,SortedSet<String>> close(Project[] projects, boolean notifyUI) {
            
            for (int i = 0; i < projects.length; i++) {
                SortedSet<String> projectOpenFiles = urls4project.get(projects [i]);
                if (projectOpenFiles != null) {
                    projectOpenFiles.retainAll (openFiles);
                    urls4project.put (projects [i], projectOpenFiles);
                    for (String url : projectOpenFiles) {
                        FileObject fo = null;
                        try {
                            fo = URLMapper.findFileObject (new URL (url));
                            openFiles.remove (fo.getURL ().toExternalForm ());
                        } catch (MalformedURLException mue) {
                            fail ("MalformedURLException in " + url);
                        } catch (FileStateInvalidException fsie) {
                            fail ("FileStateInvalidException in " + fo);
                        }
                    }
                }
            }
            
            return urls4project;
        }
    }
    
    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook 
    implements Runnable {
        
        public static int opened = 0;
        public static int closed = 0;
        
        private Object result;
        
        protected void projectClosed() {
            closed++;
            assertFalse("Working on", OpenProjects.getDefault().openProjects().isDone());
            RequestProcessor.getDefault().post(this).waitFinished();
            assertNotNull("some result computed", result);
            assertEquals("It is time out exception", TimeoutException.class, result.getClass());
        }
        
        protected void projectOpened() {
            opened++;
            assertFalse("Working on", OpenProjects.getDefault().openProjects().isDone());
            RequestProcessor.getDefault().post(this).waitFinished();
            assertNotNull("some result computed", result);
            assertEquals("It is time out exception", TimeoutException.class, result.getClass());
        }
        
        public void run() {
            try {
                result = OpenProjects.getDefault().openProjects().get(100, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                result = ex;
    }
        }
    }
    
    private class ChangeListener implements PropertyChangeListener {
        int oldCount = -1;
        int newCount = -1;

        public void propertyChange(PropertyChangeEvent arg0) {
            if (OpenProjectList.PROPERTY_OPEN_PROJECTS.equals(arg0.getPropertyName())) {
                Object old = arg0.getOldValue();
                Object nw = arg0.getNewValue();
                assertNotNull(old);
                assertNotNull(nw);
                Project[] oList = (Project[])old;
                Project[] nList = (Project[])nw;
                oldCount = oList.length;
                newCount = nList.length;
            }
        }
    }
}
