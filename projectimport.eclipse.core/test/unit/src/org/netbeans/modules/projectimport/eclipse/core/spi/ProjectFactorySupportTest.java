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
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.projectimport.eclipse.core.DotClassPath;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProjectReference;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProjectTestUtils;
import org.netbeans.modules.projectimport.eclipse.core.ProjectFactory;
import org.netbeans.modules.projectimport.eclipse.core.ProjectImporterTestCase;
import org.netbeans.modules.projectimport.eclipse.core.Workspace;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.test.MockLookup;

public class ProjectFactorySupportTest extends NbTestCase {
    
    public ProjectFactorySupportTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(new Repository(FileUtil.createMemoryFileSystem())); // otherwise have problems parsing copylibstask lib definition
    }

    private static EclipseProject getTestableProject(int version, File proj) throws IOException {
        return getTestableProject(version, proj, null, null);
    }
    
    private static EclipseProject getTestableProject(int version, File proj, Workspace w, String name) throws IOException {
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
        } else if (version == 4) {
            classpath = Arrays.asList(new DotClassPathEntry[]{
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/a-project/lib/bsh.jar",
                        "sourcepath", "/a-project/lib/bsh-sources.zip",
                        "javadoc_location", "jar:platform:/resource/a-project/lib/bsh-javadoc.jar!/doc/api"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "var",
                        "path", "REPO/lib/bsh.jar",
                        "sourcepath", "REPO/lib/bsh-sources.zip",
                        "javadoc_location", "file:/home/commons/doc/api/"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/a-folder/lib/bsh.jar",
                        "sourcepath", "/a-folder/lib/bsh-sources.zip",
                        "javadoc_location", "jar:file:/a-folder/lib/bsh-javadoc.jar!/doc/api"),
            });
        }
        List<DotClassPathEntry> sources = Arrays.asList(new DotClassPathEntry[]{
            EclipseProjectTestUtils.createDotClassPathEntry(
                    "kind", "src",
                    "path", "src"),
            EclipseProjectTestUtils.createDotClassPathEntry(
                    "kind", "src",
                    "path", "test"),
        });
        DotClassPathEntry output = null;
        DotClassPathEntry jre = null;
        DotClassPath dcp = new DotClassPath(classpath, sources, output, jre);
        File f = new File(proj, "eclipse");
        f.mkdir();
        new File(f,"src").mkdir();
        return EclipseProjectTestUtils.createEclipseProject(f, dcp, w, name);
    }
    
    public void testCalculateKey() throws IOException {
        EclipseProject eclipse = getTestableProject(1, getWorkDir());
        ProjectImportModel model = new ProjectImportModel(eclipse, new File(getWorkDirPath(), "nb"), JavaPlatform.getDefault(), Collections.<Project>emptyList());
        String expResult = 
            "src=src;" +
            "src=test;" +
            "var=MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar;" +
            "file=/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar;" +
            "prj=JavaLibrary1;"+
            "jre="+JavaPlatform.getDefault().getDisplayName()+";";
        String result = ProjectFactorySupport.calculateKey(model);
        assertEquals(expResult, result);
    }

    public void testUpdateProjectClassPath() throws IOException {
        EclipseProject eclipse = getTestableProject(1, getWorkDir());
        File prj = new File(getWorkDirPath(), "nb");
        // create required project
        AntProjectHelper helper0 = J2SEProjectGenerator.createProject(
                new File(prj, "JavaLibrary1"), "JavaLibrary1", new File[0], new File[0], null, null, null);
        Project p0 = ProjectManager.getDefault().findProject(helper0.getProjectDirectory());
        AntProjectHelper helper00 = J2SEProjectGenerator.createProject(
                new File(prj, "jlib"), "jlib", new File[0], new File[0], null, null, null);
        Project p00 = ProjectManager.getDefault().findProject(helper00.getProjectDirectory());
        ProjectImportModel model = new ProjectImportModel(eclipse, new File(prj, "test"),
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                new File(prj, "test"), "test", model.getEclipseSourceRootsAsFileArray(), 
                model.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        J2SEProject p = (J2SEProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        List<String> importProblems = new ArrayList<String>();
        ProjectFactorySupport.updateProjectClassPath(helper, p.getReferenceHelper(), model, importProblems);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${file.reference.commons-cli-1.0.jar}:" +
            "${file.reference.ejb3-persistence.jar}:" +
            "${reference.JavaLibrary1.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
        assertEquals("${var.MAVEN_REPOPO}/commons-cli/commons-cli/1.0/commons-cli-1.0.jar",
                ep.getProperty("file.reference.commons-cli-1.0.jar"));
    }
    
    public void testSynchronizeProjectClassPath() throws IOException {
        // ================= start of copy of testUpdateProjectClassPath
        EclipseProject eclipse = getTestableProject(1, getWorkDir());
        File prj = new File(getWorkDirPath(), "nb");
        // create required project
        AntProjectHelper helper0 = J2SEProjectGenerator.createProject(
                new File(prj, "JavaLibrary1"), "JavaLibrary1", new File[0], new File[0], null, null, null);
        Project p0 = ProjectManager.getDefault().findProject(helper0.getProjectDirectory());
        AntProjectHelper helper00 = J2SEProjectGenerator.createProject(
                new File(prj, "jlib"), "jlib", new File[0], new File[0], null, null, null);
        Project p00 = ProjectManager.getDefault().findProject(helper00.getProjectDirectory());
        ProjectImportModel model = new ProjectImportModel(eclipse, new File(prj, "test"),
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                new File(prj, "test"), "test", model.getEclipseSourceRootsAsFileArray(), 
                model.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        J2SEProject p = (J2SEProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        List<String> importProblems = new ArrayList<String>();
        ProjectFactorySupport.updateProjectClassPath(helper, p.getReferenceHelper(), model, importProblems);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${file.reference.commons-cli-1.0.jar}:" +
            "${file.reference.ejb3-persistence.jar}:" +
            "${reference.JavaLibrary1.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
        // ================= end of copy of testUpdateProjectClassPath
        
        String oldKey = ProjectFactorySupport.calculateKey(model);
        assertEquals(
            "src=src;" +
            "src=test;" +
            "var=MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar;" +
            "file=/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar;" +
            "prj=JavaLibrary1;"+
            "jre="+JavaPlatform.getDefault().getDisplayName()+";", oldKey);
        
        // add some items to classpath:
        eclipse = getTestableProject(2, getWorkDir());
        model = new ProjectImportModel(eclipse, new File(prj, "test"),
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        String newKey = ProjectFactorySupport.calculateKey(model);
        assertEquals("src=src;" +
            "src=test;" +
            "var=MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar;" +
            "var=MAVEN_REPOPO/some/other.jar;" +
            "file=/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar;" +
            "file=/some/other.jar;" +
            "ant=libs.david.classpath;" +
            "prj=JavaLibrary1;" +
            "prj=jlib;"+
            "jre="+JavaPlatform.getDefault().getDisplayName()+";", newKey);
        ProjectFactorySupport.synchronizeProjectClassPath(p, helper, p.getReferenceHelper(), model, oldKey, newKey, importProblems);
        ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${file.reference.commons-cli-1.0.jar}:" +
            "${file.reference.ejb3-persistence.jar}:" +
            "${reference.JavaLibrary1.jar}:" +
            "${file.reference.other.jar}:" +
            "${file.reference.other.jar-1}:" +
            "${libs.david.classpath}:" +
            "${reference.jlib.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
        assertEquals("${var.MAVEN_REPOPO}/commons-cli/commons-cli/1.0/commons-cli-1.0.jar",
                ep.getProperty("file.reference.commons-cli-1.0.jar"));
        assertEquals("${var.MAVEN_REPOPO}/some/other.jar",
                ep.getProperty("file.reference.other.jar"));
        
        oldKey = newKey;
        // remove some items from classpath:
        eclipse = getTestableProject(3, getWorkDir());
        model = new ProjectImportModel(eclipse, new File(prj, "test"),
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[]{p0, p00}));
        newKey = ProjectFactorySupport.calculateKey(model);
        assertEquals("src=src;" +
            "src=test;" +
            "var=MAVEN_REPOPO/some/other.jar;" +
            "file=/some/other.jar;" +
            "ant=libs.david.classpath;" +
            "prj=jlib;"+
            "jre="+JavaPlatform.getDefault().getDisplayName()+";", newKey);
        ProjectFactorySupport.synchronizeProjectClassPath(p, helper, p.getReferenceHelper(), model, oldKey, newKey, importProblems);
        ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(
            "${file.reference.other.jar}:" +
            "${file.reference.other.jar-1}:" +
            "${libs.david.classpath}:" +
            "${reference.jlib.jar}", 
            ep.getProperty("javac.classpath").replace(';', ':'));
        assertNull(ep.getProperty("file.reference.commons-cli-1.0.jar"));
        assertEquals("${var.MAVEN_REPOPO}/some/other.jar",
                ep.getProperty("file.reference.other.jar"));
    }
    
    public void testUpdateProjectClassPathForNonExistingRequiredProject() throws IOException {
        EclipseProject eclipse = getTestableProject(1, getWorkDir());
        File prj = new File(getWorkDirPath(), "nb");
        ProjectImportModel model = new ProjectImportModel(eclipse, new File(prj, "test"),
                JavaPlatform.getDefault(), Arrays.<Project>asList(new Project[0]));
        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                new File(prj, "test"), "test", model.getEclipseSourceRootsAsFileArray(), 
                model.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        J2SEProject p = (J2SEProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        List<String> importProblems = new ArrayList<String>();
        ProjectFactorySupport.updateProjectClassPath(helper, p.getReferenceHelper(), model, importProblems);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // required project "JavaLibrary1" is not available and therefore should not be
        // on classpath nor in key
        assertEquals(
            "${file.reference.commons-cli-1.0.jar}:" +
            "${file.reference.ejb3-persistence.jar}",
            ep.getProperty("javac.classpath").replace(';', ':'));
        String oldKey = ProjectFactorySupport.calculateKey(model);
        assertEquals(
            "src=src;" +
            "src=test;" +
            "var=MAVEN_REPOPO/commons-cli/commons-cli/1.0/commons-cli-1.0.jar;" +
            "file=/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar;" +
            "jre="+JavaPlatform.getDefault().getDisplayName()+";", oldKey);
    }

    public void testTransitiveDependencies() throws Exception {
        File transdep = ProjectImporterTestCase.extractToWorkDir("transitive-dependencies.zip", this);
        EclipseProject prj_a = ProjectFactory.getInstance().load(new File(transdep, "a"));
        ProjectImportModel mdl_a = new ProjectImportModel(prj_a, null, null, null);
        AntProjectHelper aph_a = J2SEProjectGenerator.createProject(
                new File(getWorkDir(), "a"), "a", mdl_a.getEclipseSourceRootsAsFileArray(),
                mdl_a.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        EclipseProject prj_b = ProjectFactory.getInstance().load(new File(transdep, "b"));
        ProjectImportModel mdl_b = new ProjectImportModel(prj_b, null, null, null);
        AntProjectHelper aph_b = J2SEProjectGenerator.createProject(
                new File(getWorkDir(), "b"), "b", mdl_b.getEclipseSourceRootsAsFileArray(),
                mdl_b.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        EclipseProject prj_c = ProjectFactory.getInstance().load(new File(transdep, "c"));
        ProjectImportModel mdl_c = new ProjectImportModel(prj_c, null, null, Arrays.asList(
                ProjectManager.getDefault().findProject(aph_a.getProjectDirectory()),
                ProjectManager.getDefault().findProject(aph_b.getProjectDirectory())));
        AntProjectHelper aph_c = J2SEProjectGenerator.createProject(
                new File(getWorkDir(), "c"), "c", mdl_c.getEclipseSourceRootsAsFileArray(),
                mdl_c.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        J2SEProject j2seprj_c = (J2SEProject) ProjectManager.getDefault().findProject(aph_c.getProjectDirectory());
        EclipseProjectReference.write(j2seprj_c, new EclipseProjectReference(j2seprj_c, new File(transdep, "c").getAbsolutePath(), null, 0L, ""));
        EclipseProject prj_d = ProjectFactory.getInstance().load(new File(transdep, "d"));
        ProjectImportModel mdl_d = new ProjectImportModel(prj_d, null, null, Arrays.asList(
                ProjectManager.getDefault().findProject(aph_a.getProjectDirectory()),
                ProjectManager.getDefault().findProject(aph_b.getProjectDirectory()),
                j2seprj_c));
        AntProjectHelper aph_d = J2SEProjectGenerator.createProject(
                new File(getWorkDir(), "d"), "d", mdl_d.getEclipseSourceRootsAsFileArray(),
                mdl_d.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        List<String> problems = new ArrayList<String>();
        J2SEProject j2seprj_d = (J2SEProject) ProjectManager.getDefault().findProject(aph_d.getProjectDirectory());
        ProjectFactorySupport.updateProjectClassPath(aph_d, j2seprj_d.getReferenceHelper(), mdl_d, problems);
        assertEquals(Collections.emptyList(), problems);
        assertEquals("${reference.c.jar}:${reference.b.jar}", aph_d.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).get("javac.classpath"));
    }

    public void testParseSourcesAndJavadoc() throws Exception {
        File repo = new File(getWorkDir(), "repo");
        repo.mkdir();
        File w = new File(getWorkDir(), "workspace");
        w.mkdir();
        System.err.println("repo="+repo.getPath());
        Workspace workspace = EclipseProjectTestUtils.createWorkspace(w, new Workspace.Variable("REPO", repo.getPath()));
        File f = new File(getWorkDir(), "a-project");
        f.mkdir();
        DotClassPath dcp = new DotClassPath(new ArrayList<DotClassPathEntry>(), new ArrayList<DotClassPathEntry>(), null, null);
        EclipseProject eclipse2 = EclipseProjectTestUtils.createEclipseProject(f, dcp, workspace, "a-project");
        EclipseProject eclipse = getTestableProject(4, getWorkDir(), workspace, "test");

        DotClassPathEntry e = eclipse.getClassPathEntries().get(0);
        assertEquals(f.getAbsolutePath()+"/lib/bsh.jar", e.getAbsolutePath());
        assertEquals(f.getAbsolutePath()+"/lib/bsh-sources.zip", e.getProperty("sourcepath"));
        assertEquals(f.getAbsolutePath()+"/lib/bsh-javadoc.jar", e.getProperty("javadoc_location"));
        
        e = eclipse.getClassPathEntries().get(1);
        assertEquals(repo.getAbsolutePath()+"/lib/bsh.jar", e.getAbsolutePath());
        assertEquals("${REPO}/lib/bsh-sources.zip", e.getProperty("sourcepath"));
        assertEquals("/home/commons/doc/api", e.getProperty("javadoc_location"));

        e = eclipse.getClassPathEntries().get(2);
        assertEquals("/a-folder/lib/bsh.jar", e.getAbsolutePath());
        assertEquals("/a-folder/lib/bsh-sources.zip", e.getProperty("sourcepath"));
        assertEquals("/a-folder/lib/bsh-javadoc.jar!/doc/api/", e.getProperty("javadoc_location"));
    }
    
}
