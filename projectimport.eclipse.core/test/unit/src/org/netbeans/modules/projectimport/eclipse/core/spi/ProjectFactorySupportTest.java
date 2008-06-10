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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.projectimport.eclipse.core.DotClassPath;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProjectTestUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

public class ProjectFactorySupportTest extends NbTestCase {
    
    public ProjectFactorySupportTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private EclipseProject getTestableProject(int version, File proj) {
        List<DotClassPathEntry> classpath = null;
        if (version == 1) {
            classpath = Arrays.asList(new DotClassPathEntry[]{
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "var",
                        "path", "MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "con",
                        "path", "org.eclipse.jdt.junit.JUNIT_CONTAINER/3"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "src",
                        "path", "/JavaLibrary1"),
            });
        } else if (version == 2) {
            classpath = Arrays.asList(new DotClassPathEntry[]{
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "var",
                        "path", "MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "var",
                        "path", "MAVEN_REPOPO/some/other.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/some/other.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "con",
                        "path", "org.eclipse.jdt.junit.JUNIT_CONTAINER/3"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "con",
                        "path", "org.eclipse.jdt.USER_LIBRARY/david"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "src",
                        "path", "/JavaLibrary1"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "src",
                        "path", "/jlib"),
            });
        } else if (version == 3) {
            classpath = Arrays.asList(new DotClassPathEntry[]{
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "var",
                        "path", "MAVEN_REPOPO/some/other.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/some/other.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "con",
                        "path", "org.eclipse.jdt.USER_LIBRARY/david"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "src",
                        "path", "/jlib"),
            });
        }
        List<DotClassPathEntry> sources = Arrays.asList(new DotClassPathEntry[]{
            EclipseProjectTestUtils.createDotClassPathEntry(
                    "kind", "src",
                    "path", "src")});
        DotClassPathEntry output = null;
        DotClassPathEntry jre = null;
        DotClassPath dcp = new DotClassPath(classpath, sources, output, jre);
        File f = new File(proj, "eclipse");
        f.mkdir();
        new File(f,"src").mkdir();
        return EclipseProjectTestUtils.createEclipseProject(f, dcp);
    }
    
    public void testCalculateKey() throws IOException {
        EclipseProject eclipse = getTestableProject(1, getWorkDir());
        ProjectImportModel model = new ProjectImportModel(eclipse, getWorkDirPath()+File.separator+"nb", JavaPlatform.getDefault(), Collections.<Project>emptyList());
        String expResult = 
            "src=src;" +
            "var=MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar;" +
            "file=/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar;" +
            "ant=libs.junit.classpath;" +
            "prj=JavaLibrary1;";
        String result = ProjectFactorySupport.calculateKey(model);
        assertEquals(expResult, result);
    }

    public void testUpdateProjectClassPath() throws IOException {
        EclipseProject eclipse = getTestableProject(1, getWorkDir());
        File prj = new File(getWorkDirPath()+File.separator+"nb");
        // create required project
        AntProjectHelper helper0 = J2SEProjectGenerator.createProject(
                new File(prj, "JavaLibrary1"), "JavaLibrary1", new File[0], new File[0], null, null, null);
        Project p0 = ProjectManager.getDefault().findProject(helper0.getProjectDirectory());
        AntProjectHelper helper00 = J2SEProjectGenerator.createProject(
                new File(prj, "jlib"), "jlib", new File[0], new File[0], null, null, null);
        Project p00 = ProjectManager.getDefault().findProject(helper00.getProjectDirectory());
        ProjectImportModel model = new ProjectImportModel(eclipse, prj.getAbsolutePath()+File.separator+"test", 
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                new File(prj, "test"), "test", model.getEclipseSourceRootsAsFileArray(), 
                model.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        
        List<String> importProblems = new ArrayList<String>();
        ProjectFactorySupport.updateProjectClassPath(helper, model, importProblems);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${var.MAVEN_REPOPO}/commons-cli/commons-cli/1.0/commons-cli-1.0.jar:" +
            "${file.reference.ejb3-persistence.jar}:" +
            "${libs.junit.classpath}:" +
            "${reference.JavaLibrary1.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
    }
    
    public void testSynchronizeProjectClassPath() throws IOException {
        // ================= start of copy of testUpdateProjectClassPath
        EclipseProject eclipse = getTestableProject(1, getWorkDir());
        File prj = new File(getWorkDirPath()+File.separator+"nb");
        // create required project
        AntProjectHelper helper0 = J2SEProjectGenerator.createProject(
                new File(prj, "JavaLibrary1"), "JavaLibrary1", new File[0], new File[0], null, null, null);
        Project p0 = ProjectManager.getDefault().findProject(helper0.getProjectDirectory());
        AntProjectHelper helper00 = J2SEProjectGenerator.createProject(
                new File(prj, "jlib"), "jlib", new File[0], new File[0], null, null, null);
        Project p00 = ProjectManager.getDefault().findProject(helper00.getProjectDirectory());
        ProjectImportModel model = new ProjectImportModel(eclipse, prj.getAbsolutePath()+File.separator+"test", 
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                new File(prj, "test"), "test", model.getEclipseSourceRootsAsFileArray(), 
                model.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        
        List<String> importProblems = new ArrayList<String>();
        ProjectFactorySupport.updateProjectClassPath(helper, model, importProblems);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${var.MAVEN_REPOPO}/commons-cli/commons-cli/1.0/commons-cli-1.0.jar:" +
            "${file.reference.ejb3-persistence.jar}:" +
            "${libs.junit.classpath}:" +
            "${reference.JavaLibrary1.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
        // ================= end of copy of testUpdateProjectClassPath
        
        Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        String oldKey = ProjectFactorySupport.calculateKey(model);
        assertEquals(
            "src=src;" +
            "var=MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar;" +
            "file=/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar;" +
            "ant=libs.junit.classpath;" +
            "prj=JavaLibrary1;", oldKey);
        
        // add some items to classpath:
        eclipse = getTestableProject(2, getWorkDir());
        model = new ProjectImportModel(eclipse, prj.getAbsolutePath()+File.separator+"test", 
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        String newKey = ProjectFactorySupport.calculateKey(model);
        assertEquals("src=src;" +
            "var=MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar;" +
            "var=MAVEN_REPOPO/some/other.jar;" +
            "file=/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar;" +
            "file=/some/other.jar;" +
            "ant=libs.junit.classpath;" +
            "ant=libs.david.classpath;" +
            "prj=JavaLibrary1;" +
            "prj=jlib;", newKey);
        ProjectFactorySupport.synchronizeProjectClassPath(p, helper, model, oldKey, newKey, importProblems);
        ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${var.MAVEN_REPOPO}/commons-cli/commons-cli/1.0/commons-cli-1.0.jar:" +
            "${file.reference.ejb3-persistence.jar}:" +
            "${libs.junit.classpath}:" +
            "${reference.JavaLibrary1.jar}:" +
            "${var.MAVEN_REPOPO}/some/other.jar:" +
            "${file.reference.other.jar}:" +
            "${libs.david.classpath}:" +
            "${reference.jlib.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
        
        oldKey = newKey;
        // remove some items from classpath:
        eclipse = getTestableProject(3, getWorkDir());
        model = new ProjectImportModel(eclipse, prj.getAbsolutePath()+File.separator+"test", 
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        newKey = ProjectFactorySupport.calculateKey(model);
        assertEquals("src=src;" +
            "var=MAVEN_REPOPO/some/other.jar;" +
            "file=/some/other.jar;" +
            "ant=libs.david.classpath;" +
            "prj=jlib;", newKey);
        ProjectFactorySupport.synchronizeProjectClassPath(p, helper, model, oldKey, newKey, importProblems);
        ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${var.MAVEN_REPOPO}/some/other.jar:" +
            "${file.reference.other.jar}:" +
            "${libs.david.classpath}:" +
            "${reference.jlib.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
        
    }
}
