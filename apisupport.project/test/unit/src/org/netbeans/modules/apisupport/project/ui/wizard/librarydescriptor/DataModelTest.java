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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Tests {@link DataModel}.
 *
 * @author Radek Matous
 */
public class DataModelTest extends LayerTestBase {
    public DataModelTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        LibraryManager lbm = LibraryManager.getDefault();
    }
    
    public void testValidityOfDataModel() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        NewLibraryDescriptor.DataModel data = new NewLibraryDescriptor.DataModel(wd);
        
        assertEquals(project, data.getProject());
        
        assertFalse(data.isValidLibraryDisplayName());
        assertFalse(data.isValidLibraryName());
        
        data.setLibraryName("");
        assertFalse(data.isValidLibraryName());

        data.setLibraryDisplayName("");
        assertFalse(data.isValidLibraryDisplayName());
        
        data.setLibraryName("mylibrary");
        assertTrue(data.isValidLibraryName());

        data.setLibraryDisplayName("mylibrary is great");
        assertTrue(data.isValidLibraryDisplayName());
        
        
        assertFalse(data.libraryAlreadyExists());
        LayerUtils.LayerHandle h = LayerUtils.layerForProject(data.getProject());
        FileSystem fs = h.layer(true);
        FileObject fo = FileUtil.createData(fs.getRoot(),CreatedModifiedFilesProvider.getLibraryDescriptorEntryPath(data.getLibraryName()));
        assertNotNull(fo);
        assertTrue(data.libraryAlreadyExists());
    }    
}

