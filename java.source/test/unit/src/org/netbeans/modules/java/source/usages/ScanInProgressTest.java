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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openidex.search.SearchInfo;

import java.awt.EventQueue;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.project.ui.ExtIcon;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.modules.project.ui.ProjectsRootNode;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ScanInProgressTest extends NbTestCase implements PropertyChangeListener {
    CountDownLatch first;
    CountDownLatch middle;
    CountDownLatch rest;
    private int events;
    private static boolean result = true;
    
    public ScanInProgressTest(String testName) {
        super(testName);
    }

    Lookup createLookup(TestSupport.TestProject project, Object instance) {
        return Lookups.singleton(instance);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        MockServices.setServices(TestSupport.TestProjectFactory.class);

        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);

        first = new CountDownLatch(1);
        middle = new CountDownLatch(1);
        rest = new CountDownLatch(2);

        List<URL> list = new ArrayList<URL>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            FileObject prj = TestSupport.createTestProject(workDir, "prj" + i);
            URL url = URLMapper.findURL(prj, URLMapper.EXTERNAL);
            list.add(url);
            names.add(url.toExternalForm());
            icons.add(new ExtIcon());
            TestSupport.TestProject tmp = (TestSupport.TestProject)ProjectManager.getDefault ().findProject (prj);
            assertNotNull("Project found", tmp);
            CountDownLatch down = i == 0 ? first : (i == 5 ? middle : rest);
            tmp.setLookup(createLookup(tmp, new TestProjectOpenedHookImpl(down, prj)));
        }

        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);

        OpenProjects.getDefault().addPropertyChangeListener(this);
    }

    public void testScanInProgressWhenOpeningProject() throws InterruptedException {
        assertEquals("No events in API", 0, events);

        Node logicalView = new ProjectsRootNode(1 /*ProjectsRootNode.LOGICAL_VIEW*/);
        L listener = new L();
        logicalView.addNodeListener(listener);

        assertEquals("No events in API", 0, events);
        assertEquals("10 children", 10, logicalView.getChildren().getNodesCount());
        listener.assertEvents("None", 0);
        assertEquals("No project opened yet", 0, TestProjectOpenedHookImpl.opened);
        assertEquals("No events in API", 0, events);

        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        assertEquals("No events in API", 0, events);

        Node midNode = logicalView.getChildren().getNodes()[5];
        {
            TestSupport.TestProject p = midNode.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        Project lazyP = midNode.getLookup().lookup(Project.class);
        assertNotNull("Some project is found", lazyP);
//        assertEquals("It is lazy project", LazyProject.class, lazyP.getClass());
        assertEquals("No events in API", 0, events);

        middle.countDown();
        // not necessary, but to ensure middle really does not run
        Thread.sleep(300);
        assertEquals("Still no processing", 0, TestProjectOpenedHookImpl.opened);
        // trigger initialization of the node, shall trigger OpenProjectList.preferredProject(lazyP);
        midNode.getChildren().getNodes();
        first.countDown();

        TestProjectOpenedHookImpl.toOpen.await();

        {
            TestSupport.TestProject p = null;
            for (int i = 0; i < 10; i++) {
                Node midNode2 = logicalView.getChildren().getNodes()[5];
                p = midNode.getLookup().lookup(TestSupport.TestProject.class);
                if (p != null) {
                    break;
                }
                Thread.sleep(100);
            }
            assertNotNull("The right project opened", p);
        }
        assertEquals("No events in API", 0, events);

        {
            int cnt = 0;
            for (int i = 0; i < 10; i++) {
                Node n = logicalView.getChildren().getNodes()[i];
                TestSupport.TestProject p = null;
                p = n.getLookup().lookup(TestSupport.TestProject.class);
                if (p != null) {
                    cnt++;
                }
            }
            assertEquals("First and fifth projects are open, nobody else is", 2, cnt);
        }

        assertEquals("No events in API", 0, events);
        rest.countDown();
        rest.countDown();

        //OpenProjectList.waitProjectsFullyOpen();
        //assertEquals("Finally notified in API", 1, events);
        SourceUtils.waitScanFinished();
        assertEquals("All projects opened", 10, TestProjectOpenedHookImpl.opened);


        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNotNull("Nodes have correct project of this type", p);
            SearchInfo s = n.getLookup().lookup(SearchInfo.class);
            assertNotNull("Nodes have correct project of this type", s);
        }
        assertFalse(IndexingManager.getDefault().isIndexing());
        assertTrue(result);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            events++;
        }
    }

    private static class L implements NodeListener, PropertyChangeListener {
        public List<EventObject> events = new ArrayList<EventObject>();

        public void childrenAdded(NodeMemberEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void childrenRemoved(NodeMemberEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void childrenReordered(NodeReorderEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void nodeDestroyed(NodeEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(evt);
        }

        final void assertEvents(String string, int i) {
            assertEquals(string + events, i, events.size());
            events.clear();
        }

    }

    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook {

        public static CountDownLatch toOpen = new CountDownLatch(2);
        public static int opened = 0;
        public static int closed = 0;


        private CountDownLatch toWaitOn;
        private FileObject prj;

        public TestProjectOpenedHookImpl(CountDownLatch toWaitOn, FileObject prj) {
            this.toWaitOn = toWaitOn;
            this.prj = prj;
        }

        protected void projectClosed() {
            closed++;
        }

        protected void projectOpened() {
            ClassPath cp = ClassPathSupport.createClassPath(prj);
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { cp });

            if (toWaitOn != null) {
                try {
                    toWaitOn.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            opened++;
            toOpen.countDown();
            result &= IndexingManager.getDefault().isIndexing();
            assertTrue(result);
        }

    }
}

