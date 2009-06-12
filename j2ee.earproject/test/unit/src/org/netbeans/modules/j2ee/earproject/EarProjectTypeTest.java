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
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 * @author vkraemer
 */
public final class EarProjectTypeTest extends NbTestCase {
    
    private AntBasedProjectType prjType;
    
    public EarProjectTypeTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockLookup.init();
        Collection<? extends AntBasedProjectType> all = Lookups.forPath("Services/AntBasedProjectTypes").lookupAll(AntBasedProjectType.class);
        prjType = null;
        for (AntBasedProjectType instance : all) {
            if ("org.netbeans.modules.j2ee.earproject".equals(instance.getType()))
            prjType = instance;
        }
        MockLookup.setLayersAndInstances(prjType);
        assertNotNull(prjType);
    }
    
    public void testCreateProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "EarProjectTypeTest.testCreatProject");
        FileUtil.createFolder(prjDirF);
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        AntProjectHelper tmp = ProjectGenerator.createProject(prjDirFO, prjType.getType());
        prjType.createProject(tmp);
    }
    
    public void testCreateProjectNullArg() throws Exception {
        try {
            prjType.createProject(null);
            fail("null is an invalid argument");
        } catch (NullPointerException ex) {
            // OK we should get here
        }
    }
    
    public void testGetType() {
        assertEquals(prjType.getType(), "org.netbeans.modules.j2ee.earproject");
    }
    
}
