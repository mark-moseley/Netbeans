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

package org.netbeans.modules.project.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.Action;
import junit.framework.TestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Handle;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/** 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectsRootNodeTest extends NbTestCase {
    CountDownLatch down;
    
    public ProjectsRootNodeTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        
        down = new CountDownLatch(1);
        
        List<URL> list = new ArrayList<URL>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < 30; i++) {
            FileObject prj = TestSupport.createTestProject(workDir, "prj" + i);
            URL url = URLMapper.findURL(prj, URLMapper.EXTERNAL);
            list.add(url);
            names.add(url.toExternalForm());
            icons.add(new ExtIcon());
            TestSupport.TestProject tmp = (TestSupport.TestProject)ProjectManager.getDefault ().findProject (prj);
            assertNotNull("Project found", tmp);
            tmp.setLookup(Lookups.singleton(new TestProjectOpenedHookImpl(down)));
        }
        
        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBehaviourOfProjectsLogicNode() throws InterruptedException {
        Node logicalView = new ProjectsRootNode(ProjectsRootNode.LOGICAL_VIEW);
        L listener = new L();
        logicalView.addNodeListener(listener);
        
        assertEquals("30 children", 30, logicalView.getChildren().getNodesCount());
        listener.assertEvents("None", 0);
        assertEquals("No project opened yet", 0, TestProjectOpenedHookImpl.opened);
        
        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        
        // let project open code run
        down.countDown();
        TestProjectOpenedHookImpl.toOpen.await();
        
        assertEquals("All projects opened", 30, TestProjectOpenedHookImpl.opened);
        
        OpenProjectList.waitProjectsFullyOpen();

        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNotNull("Nodes have correct project of this type", p);
        }
        
        listener.assertEvents("Goal is to receive no events at all", 0);
    }
    
    private static class L implements NodeListener {
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
        
        public static CountDownLatch toOpen = new CountDownLatch(30);
        public static int opened = 0;
        public static int closed = 0;
        
        
        private CountDownLatch toWaitOn;
        
        public TestProjectOpenedHookImpl(CountDownLatch toWaitOn) {
            this.toWaitOn = toWaitOn;
        }
        
        protected void projectClosed() {
            closed++;
        }
        
        protected void projectOpened() {
            if (toWaitOn != null) {
                try {
                    toWaitOn.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            opened++;
            toOpen.countDown();
        }
        
    }
}
