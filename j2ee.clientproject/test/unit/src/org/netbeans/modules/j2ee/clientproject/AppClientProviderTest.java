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

package org.netbeans.modules.j2ee.clientproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.common.project.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Andrei Badea
 */
public class AppClientProviderTest extends NbTestCase {
    
    private static final String APPLICATION_CLIENT_XML = "application-client.xml";
    
    public AppClientProviderTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        AntProjectHelper helper = ((AppClientProject) project).getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensuer application-client.xml exists
        FileObject appXmlFO = metaInfFO.getFileObject(APPLICATION_CLIENT_XML);
        assertNotNull(appXmlFO);
        
        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        assertEquals(FileUtil.toFile(metaInfFO.getFileObject(APPLICATION_CLIENT_XML)),
                provider.getJ2eeModule().getDeploymentConfigurationFile(APPLICATION_CLIENT_XML));
    }
    
    public void testMetadataModel() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = provider.getJ2eeModule();
        assertNotNull(j2eeModule.getMetadataModel(AppClientMetadata.class));
        assertNotNull(j2eeModule.getMetadataModel(WebservicesMetadata.class));
    }
    
    public void testThatProjectWithoutDDCanBeOpened() throws Exception {
        File prjDirOrigF = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        File prjDirF = TestUtil.copyFolder(getWorkDir(), prjDirOrigF);
        TestUtil.deleteRec(new File(new File(prjDirF, "src"), "conf"));
        
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        
        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        File someConfFile = provider.getJ2eeModule().getDeploymentConfigurationFile("does-not-matter.xml");
        assertNotNull("J2eeModuleProvider.getDeploymentConfigurationFile() cannot return null", someConfFile);
        File expected = new File(prjDirF + File.separator + "src" +
                File.separator + "conf" + File.separator + "does-not-matter.xml");
        assertEquals("expected path", expected, someConfFile);
    }
    
    public void testNeedConfigurationFolder() {
        assertTrue("1.3 needs configuration folder",
                AppClientProvider.needConfigurationFolder(J2EEProjectProperties.J2EE_1_3));
        assertTrue("1.4 needs configuration folder",
                AppClientProvider.needConfigurationFolder(J2EEProjectProperties.J2EE_1_4));
        assertFalse("5.0 does not need configuration folder",
                AppClientProvider.needConfigurationFolder(J2EEProjectProperties.JAVA_EE_5));
        assertFalse("Anything else does not need configuration folder",
                AppClientProvider.needConfigurationFolder("5.0"));
        assertFalse("Anything else does not need configuration folder",
                AppClientProvider.needConfigurationFolder("6.0.hmmm?"));
        assertFalse("Even null does not need configuration folder",
                AppClientProvider.needConfigurationFolder(null));
    }
    
}
