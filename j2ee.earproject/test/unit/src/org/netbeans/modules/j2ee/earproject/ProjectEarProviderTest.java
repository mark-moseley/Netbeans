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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class ProjectEarProviderTest extends NbTestCase {

    private String serverID;

    public ProjectEarProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }

    public void testFindEarJavaEE() throws Exception {
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel, serverID);
        FileObject earDirFO = FileUtil.toFileObject(earDirF);
        Project createdEjbJarProject = ProjectManager.getDefault().findProject(earDirFO);
        assertNotNull("Ear found", Ear.getEar(earDirFO));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf")));
        assertNull("DD should not exist", earDirFO.getFileObject("src/conf/application.xml"));
        assertNull("not Ear for parent", Ear.getEar(earDirFO.getParent()));
    }

    public void testFindEarJ2EE() throws Exception {
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.J2EE_14;
        String ejbName = "testEA-ejb";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel, serverID);
        FileObject earDirFO = FileUtil.toFileObject(earDirF);
        Project createdEjbJarProject = ProjectManager.getDefault().findProject(earDirFO);
        assertNotNull("Ear found", Ear.getEar(earDirFO));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf/application.xml")));
        assertNull("not Ear for parent", Ear.getEar(earDirFO.getParent()));
    }

}
