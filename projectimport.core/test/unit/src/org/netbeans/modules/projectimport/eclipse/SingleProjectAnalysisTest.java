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

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.Collection;

/**
 * Tests importing of single project (that is without workspace provided).
 *
 * <p>
 * This is first level check if importer is working correctly - i.e. it is able
 * to parse project without <code>ProjectImporterException<code> and similar to
 * be thrown.
 * </p>
 *
 * @author mkrauskopf
 */
public final class SingleProjectAnalysisTest extends ProjectImporterTestCase {

    public SingleProjectAnalysisTest(String name) {
        super(name);
    }
    
    public void testSimpleAloneProjectForLatestMilestone() throws Exception {
        File projectDir = extractToWorkDir("simpleAlone-3.1M6.zip");
        EclipseProject project = ProjectFactory.getInstance().load(projectDir);
        assertNotNull(project);
        doBasicProjectTest(project, 0);
        Collection projects = project.getProjects();
        assertTrue("There are no required projects for the project.", projects.isEmpty());
    }
    
    public void testEmptyWithoutConAndSrc58033() throws Exception {
        File projectDir = extractToWorkDir("emptyWithoutConAndSrc-3.0.2.zip");
        EclipseProject project = ProjectFactory.getInstance().load(projectDir);
        assertNotNull(project);
    }
    
    static void doBasicProjectTest(EclipseProject project, int cpItemsCount) {
        /* usage (see printOtherProjects to see how to use them) */
        String name = project.getName();
        assertTrue("Name cannot be null or empty", (name != null && !name.equals("")));
        
        File directory = project.getDirectory();
        assertNotNull(directory);
        
        String jdkDir = project.getJDKDirectory();
        //        assertNotNull("Cannot resolve JDK directory \"" + jdkDir + "\"", jdkDir);
        
        Collection srcRoots = project.getSourceRoots();
        assertFalse("Tere should be at least on source root",
                srcRoots.isEmpty());
        
        Collection cp = project.getClassPathEntries();
        assertEquals(cpItemsCount, cp.size());
    }
}
