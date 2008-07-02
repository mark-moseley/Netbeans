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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ui.actions;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectapi.TimedWeakReference;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class ActiveConfigActionTest extends NbTestCase {
    
    static {
        new ActiveConfigAction.DynLayer();
         TimedWeakReference.TIMEOUT = 10;
    }
    
    public ActiveConfigActionTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockServices.setServices(PF.class);
        
        PF.toCreate = new P();
        assertEquals(PF.toCreate, ProjectManager.getDefault().findProject(PF.toCreate.fo));
    }

    private static Object holder;
    /**
     * Test of createContextAwareInstance method, of class ActiveConfigAction.
     */
    public void testCreateContextAwareInstance() {
        P p = PF.toCreate;
        
        OpenProjects.getDefault().open(new Project[] { p }, false);
        OpenProjects.getDefault().setMainProject(p);
        ActiveConfigAction instance = new ActiveConfigAction();
        Action result = instance;//.createContextAwareInstance(actionContext);
        
        assertTrue("menu: " + result, result instanceof Presenter.Menu);
        Presenter.Menu menu = (Presenter.Menu)result;
        
        JMenuItem item = menu.getMenuPresenter();
        
        
        assertNotNull(item);
        assertTrue("Enabled", item.isEnabled());
        DynamicMenuContent m = (DynamicMenuContent)item;
        assertEquals("One", 1, m.getMenuPresenters().length);
        
        holder = item;
        
        OpenProjects.getDefault().close(new Project[] { p });
        assertNull("NO project selected", OpenProjects.getDefault().getMainProject());
        
        WeakReference<Object> ref = new WeakReference<Object>(p);
        p = null;
        PF.toCreate = null;
        
        assertGC("Reference can go away", ref);
    }

    public static final class PF implements ProjectFactory {
        static P toCreate;
        
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.equals(toCreate.fo);
        }

        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            return toCreate;
        }

        public void saveProject(Project project) throws IOException, ClassCastException {
        }
    }
    
    private static class P implements Project, ProjectConfigurationProvider {
        Lookup l = Lookups.singleton(this);
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot();
        PC conf = new PC();
        
        public P() {
        }

        public FileObject getProjectDirectory() {
            return fo;
        }

        public Lookup getLookup() {
            return l;
        }

        public Collection getConfigurations() {
            return Collections.singleton(conf);
        }

        public ProjectConfiguration getActiveConfiguration() {
            return conf;
        }

        public void setActiveConfiguration(ProjectConfiguration configuration) throws IllegalArgumentException, IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasCustomizer() {
            return true;
        }

        public void customize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean configurationsAffectAction(String command) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListener(PropertyChangeListener lst) {
        }

        public void removePropertyChangeListener(PropertyChangeListener lst) {
        }
    }
    
    private static final class PC implements ProjectConfiguration {
        public String getDisplayName() {
            return "Default";
        }
        
    }
}
