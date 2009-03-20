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
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * @author Andrei Badea
 */
public class EarProviderTest extends NbTestCase {

    
    private static final String APPLICATION_XML = "application.xml";
    
    
    public EarProviderTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        MockLookup.setLayersAndInstances();
        File f = new File(getDataDir().getAbsolutePath(), "projects/EnterpriseApplication1");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        EarProject earProject = project.getLookup().lookup(EarProject.class);
        AntProjectHelper helper = earProject.getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensure application-client.xml exists
        FileObject appXmlFO = metaInfFO.getFileObject(APPLICATION_XML);
        assertNotNull(appXmlFO);
        
        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        File dcFile = provider.getJ2eeModule().getDeploymentConfigurationFile(APPLICATION_XML);
        assertEquals(FileUtil.toFile(metaInfFO.getFileObject(APPLICATION_XML)), dcFile);
        
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = jmp.getJ2eeModule();
        assertNotNull(j2eeModule.getMetadataModel(ApplicationMetadata.class));
    }
    
}
