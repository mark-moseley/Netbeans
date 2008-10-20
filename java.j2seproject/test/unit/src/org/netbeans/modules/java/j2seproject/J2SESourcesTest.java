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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.TestUtil;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 * Tests J2SESources
 * Tests if SourceForBinaryQuery works fine on external build folder.
 *
 * @author Tomas Zezula
 */
public class J2SESourcesTest extends NbTestCase {

    public J2SESourcesTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject build;
    private FileObject classes;
    private ProjectManager pm;
    private Project project;
    private AntProjectHelper helper;

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        });
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null); //NOI18N        
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        sources = this.getFileObject(projdir, "src");
        build = this.getFileObject (scratch, "build");
        classes = this.getFileObject(build,"classes");
        File f = FileUtil.normalizeFile (FileUtil.toFile(build));
        String path = f.getAbsolutePath ();
//#47657: SourcesHelper.remarkExternalRoots () does not work on deleted folders
// To reproduce it uncomment following line
//        build.delete();
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty(J2SEProjectProperties.BUILD_DIR, path);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        pm = ProjectManager.getDefault();
        project = pm.findProject(projdir);
        assertTrue("Invalid project type", project instanceof J2SEProject);
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        build = null;
        classes = null;
        pm = null;
        project = null;
        helper = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    public void testSourceRoots () throws Exception {        
        FileObject[] roots = SourceForBinaryQuery.findSourceRoots(classes.getURL()).getRoots();
        assertNotNull (roots);        
        assertEquals("There should be 1 src root",1,roots.length);
        assertTrue ("The source root is not valid", sources.isValid());
        assertEquals("Invalid src root", sources, roots[0]);               
        FileObject src2 = projdir.createFolder("src2");        
        addSourceRoot (helper, src2, "src2");        
        roots = SourceForBinaryQuery.findSourceRoots(classes.getURL()).getRoots();
        assertNotNull (roots);
        assertEquals("There should be 2 src roots", 2, roots.length);
        assertTrue ("The source root is not valid", sources.isValid());
        assertEquals("Invalid src root", sources, roots[0]);
        assertTrue ("The source root 2 is not valid", src2.isValid());
        assertEquals("Invalid src2 root", src2, roots[1]);
    }

    public void testIncludesExcludes() throws Exception {
        SourceGroup g = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)[0];
        assertEquals(sources, g.getRootFolder());
        FileObject objectJava = FileUtil.createData(sources, "java/lang/Object.java");
        FileObject jcJava = FileUtil.createData(sources, "javax/swing/JComponent.java");
        FileObject doc = FileUtil.createData(sources, "javax/swing/doc-files/index.html");
        assertTrue(g.contains(objectJava));
        assertTrue(g.contains(jcJava));
        assertTrue(g.contains(doc));
        Method projectOpened = ProjectOpenedHook.class.getDeclaredMethod("projectOpened");
        projectOpened.setAccessible(true);
        projectOpened.invoke(project.getLookup().lookup(ProjectOpenedHook.class));
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("includes/excludes were initialized to defaults", "**", ep.getProperty(ProjectProperties.INCLUDES));
        assertEquals("includes/excludes were initialized to defaults", "", ep.getProperty(ProjectProperties.EXCLUDES));
        ep.setProperty(ProjectProperties.INCLUDES, "javax/swing/");
        ep.setProperty(ProjectProperties.EXCLUDES, "**/doc-files/");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        pm.saveProject(project);
        assertFalse(g.contains(objectJava));
        assertTrue(g.contains(jcJava));
        assertFalse(g.contains(doc));
    }
    
    private static FileObject getFileObject (FileObject parent, String name) throws IOException {
        FileObject result = parent.getFileObject(name);
        if (result == null) {
            result = parent.createFolder(name);
        }
        return result;
    }   
    

    private static void addSourceRoot (AntProjectHelper helper, FileObject sourceFolder, String propName) throws Exception {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttribute("id", propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        File f = FileUtil.normalizeFile(FileUtil.toFile(sourceFolder));
        props.put (propName, f.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
    }


}
