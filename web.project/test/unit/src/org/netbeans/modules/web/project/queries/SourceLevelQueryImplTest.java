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

package org.netbeans.modules.web.project.queries;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.modules.web.project.test.TestBase;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * Tests for SourceLevelQueryImpl
 *
 * @author David Konecny, Radko Najman
 */
public class SourceLevelQueryImplTest extends NbTestCase {
    
    public SourceLevelQueryImplTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private ProjectManager pm;
    private Project pp;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.setLookup(new Object[] {
            new org.netbeans.modules.web.project.WebProjectType(),
            new org.netbeans.modules.java.project.ProjectSourceLevelQueryImpl(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation(),
            new TestPlatformProvider ()
        });
        Properties p = System.getProperties();
        if (p.getProperty ("netbeans.user") == null) {
            p.put("netbeans.user", FileUtil.toFile(TestUtil.makeScratchDir(this)).getAbsolutePath());
        }
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        pm = null;
        pp = null;
        super.tearDown();
    }
    
    
    private void prepareProject (String platformName) throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        AntProjectHelper helper = ProjectGenerator.createProject(projdir, "org.netbeans.modules.web.project");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("javac.source", "${def}");
        props.setProperty ("platform.active",platformName);
        props.setProperty("def", "1.2");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        props = PropertyUtils.getGlobalProperties();
        props.put("default.javac.source","4.3");
        PropertyUtils.putGlobalProperties(props);
        sources = projdir.createFolder("src");
    }
    
    public void testGetSourceLevelWithValidPlatform() throws Exception {
        this.prepareProject("TestPlatform");
        FileObject file = scratch.createData("some.java");
        String sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Non-project Java file does not have any source level", null, sl);
        file = sources.createData("a.java");
        sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Project's Java file must have project's source", "1.2", sl);        
    }
    
    public void testGetSourceLevelWithBrokenPlatform() throws Exception {
        this.prepareProject("BrokenPlatform");
        FileObject file = scratch.createData("some.java");
        String sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Non-project Java file does not have any source level", null, sl);
        file = sources.createData("a.java");
        sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Project's Java file must have project's source", "4.3", sl);        
    }
    
    private static class TestPlatformProvider implements JavaPlatformProvider {
        
        private JavaPlatform platform;
        
        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        public JavaPlatform[] getInstalledPlatforms()  {
            return new JavaPlatform[] {
                getDefaultPlatform()
            };
        }

        public JavaPlatform getDefaultPlatform()  {
            if (this.platform == null) {
                this.platform = new TestPlatform ();
            }
            return this.platform;
        }                                
    }
    
    private static class TestPlatform extends JavaPlatform {
        
        public FileObject findTool(String toolName) {
            return null;
        }

        public String getVendor() {
            return "me";    
        }

        public ClassPath getStandardLibraries() {
            return null;
        }

        public Specification getSpecification() {
            return new Specification ("j2se", new SpecificationVersion ("1.5"));
        }

        public ClassPath getSourceFolders() {
            return null;
        }

        public java.util.Map getProperties() {
            return Collections.singletonMap("platform.ant.name","TestPlatform");
        }

        public java.util.List getJavadocFolders() {
            return null;
        }

        public java.util.Collection getInstallFolders() {
            return null;
        }

        public String getDisplayName() {
            return "TestPlatform";
        }

        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }
        
    }
    
}
