/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.ProjectFactory;
import org.netbeans.modules.projectimport.eclipse.core.ProjectImporterTestCase;
import org.netbeans.modules.projectimport.eclipse.core.WorkspaceFactory;

public class ProjectImportModelTest extends ProjectImporterTestCase {

    public ProjectImportModelTest(String name) {
        super(name);
    }

    public void testTestRootDetection() throws Exception {
        File welltested = extractToWorkDir("welltested.zip");
        assertTrue(welltested.isDirectory());
        EclipseProject prj = ProjectFactory.getInstance().load(welltested);
        ProjectImportModel model = new ProjectImportModel(prj, null, null, null);
        assertEquals(Collections.singletonList(new File(welltested, "src")), Arrays.asList(model.getEclipseSourceRootsAsFileArray()));
        assertEquals(Collections.singletonList(new File(welltested, "test")), Arrays.asList(model.getEclipseTestSourceRootsAsFileArray()));
    }

    public void testCompilerOptions() throws Exception {
        File tco = extractToWorkDir("test-compiler-options.zip");
        EclipseProject prj = ProjectFactory.getInstance().load(tco);
        ProjectImportModel model = new ProjectImportModel(prj, null, null, null);
        assertEquals("1.5", model.getSourceLevel());
        assertEquals("1.6", model.getTargetLevel());
        assertTrue(model.isDebug());
        assertTrue(model.isDeprecation());
        assertEquals("UTF-8", model.getEncoding());
        assertEquals("-Xlint:fallthrough -Xlint:finally -Xlint:unchecked", model.getCompilerArgs().toString());
    }
    
    public void testLaunchConfigurations() throws Exception {
        File unpacked = extractToWorkDir("launch-config.zip");
        Set<EclipseProject> prjs = WorkspaceFactory.getInstance().load(unpacked).getProjects();
        EclipseProject prj = null;
        for (EclipseProject _p : prjs) {
            if (_p.getName().equals("p")) {
                prj = _p;
                break;
            }
        }
        assertNotNull(prj);
        ProjectImportModel model = new ProjectImportModel(prj, null, null, null);
        Collection<LaunchConfiguration> configs = model.getLaunchConfigurations();
        assertEquals(1, configs.size());
        LaunchConfiguration config = configs.iterator().next();
        assertEquals("Main", config.getName());
        assertEquals(LaunchConfiguration.TYPE_LOCAL_JAVA_APPLICATION, config.getType());
        assertEquals("app.Main", config.getMainType());
        assertEquals("world", config.getProgramArguments());
        assertEquals("-Dgreeting=hello", config.getVmArguments());
    }

}
