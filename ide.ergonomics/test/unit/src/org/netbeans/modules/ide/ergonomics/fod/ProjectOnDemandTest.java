/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectOnDemandTest extends NbTestCase implements PropertyChangeListener {
    FileObject root;
    private static final InstanceContent ic = new InstanceContent();

    static {
        Feature2LayerMapping.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private int change;
    
    
    public ProjectOnDemandTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(ProjectOnDemandTest.class).
            gui(false)
        );
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        ic.set(Collections.emptyList(), null);

        URI uri = ModuleInfo.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File jar = new File(uri);
        System.setProperty("netbeans.home", jar.getParentFile().getParent());
        System.setProperty("netbeans.user", getWorkDirPath());
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        Exception ex2 = null;
        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (info.getCodeNameBase().equals("org.netbeans.modules.autoupdate.ui")) {
             Method m = null;
                Class<?> c = info.getClass();
                for (;;) {
                    if (c == null) {
                        throw ex2;
                    }
                    try {
                        m = c.getDeclaredMethod("setEnabled", Boolean.TYPE);
                    } catch (Exception ex) {
                        ex2 = ex;
                    }
                    if (m != null) {
                        break;
                    }
                    c = c.getSuperclass();
                }
                m.setAccessible(true);
                m.invoke(info, false);
                assertFalse("Module is disabled", info.isEnabled());
                found = true;
            }
            sb.append(info.getCodeNameBase()).append('\n');
        }
        if (!found) {
            fail("No module found:\n" + sb);
        }

        FeatureInfo info = FeatureInfo.create(
            "cluster",
            ProjectOnDemandTest.class.getResource("FeatureInfo.xml"),
            ProjectOnDemandTest.class.getResource("TestBundle.properties")
        );
        ic.add(info);
        
        File dbp = new File(getWorkDir(), "dbproject");
        File db = new File(dbp, "project.properties");
        dbp.mkdirs();
        db.createNewFile();

        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("fileobject found", root);

        OpenProjects.getDefault().open(new Project[0], false);
        assertEquals("Empty", 0, OpenProjects.getDefault().getOpenProjects().length);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRecognizeDocBookProject() throws Exception {
        assertTrue("Recognized as project", ProjectManager.getDefault().isProject(root));
        Project p = ProjectManager.getDefault().findProject(root);
        assertNotNull("Project found", p);
        
        ProjectOpenedHook open = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("Open hook found", open);

        ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
        assertNotNull("Info about icon", info);
        assertNotNull("Icon provided", info.getIcon());
        info.addPropertyChangeListener(this);

        TestFactory.recognize = root;
        OpenProjects.getDefault().open(new Project[] { p }, false);
        
        assertEquals("No Dialog currently created", 0, DD.cnt);
        
        List<Project> arr = Arrays.asList(OpenProjects.getDefault().openProjects().get());
        assertEquals("However one instance is there", 1, arr.size());
        Project newP = arr.get(0).getLookup().lookup(Project.class);
        if (p == newP) {
            fail("New project is made available, not old: " + newP);
        }
        assertEquals("Right type", TestFactory.class, newP.getClass());
        TestFactory tf = (TestFactory)newP;
        assertEquals("Open hook called", 1, tf.opened);

        assertEquals("Project info changed", 1, change);
        assertEquals("One listener attached", 1, tf.listenerCount);
        assertEquals("Info delegates", "x", info.getName());
        assertEquals("Info delegates2", "y", info.getDisplayName());
        assertEquals("Info delegates icon", null, info.getIcon());

        OpenProjects.getDefault().close (new Project[] { newP });
        if (OpenProjects.getDefault().getOpenProjects().length != 0) {
            fail("All projects shall be closed: " + Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        change++;
    }



    public static final class DD extends DialogDisplayer {
        static int cnt = 0;
        
        @Override
        public Object notify(NotifyDescriptor descriptor) {
            cnt++;
            return NotifyDescriptor.OK_OPTION;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
