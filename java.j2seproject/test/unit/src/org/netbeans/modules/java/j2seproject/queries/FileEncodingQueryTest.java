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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.File;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class FileEncodingQueryTest extends NbTestCase {

    public FileEncodingQueryTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private AntProjectHelper helper;
    private J2SEProject prj;

    protected void setUp() throws Exception {
        ClassLoader l = this.getClass().getClassLoader();
        MockLookup.setLookup(
                Lookups.fixed(l, new DummyXMLEncodingImpl()),
                Lookups.metaInfServices(l));
        super.setUp();
        this.clearWorkDir();
        File wd = getWorkDir();
        scratch = FileUtil.toFileObject(wd);
        assertNotNull(wd);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null);
        Project p = FileOwnerQuery.getOwner(projdir);
        assertNotNull(p);
        prj = p.getLookup().lookup(J2SEProject.class);
        assertNotNull(prj);
        sources = projdir.getFileObject("src");
    }

    public void testFileEncodingQuery () throws Exception {
        final Charset UTF8 = Charset.forName("UTF-8");
        final Charset ISO15 = Charset.forName("ISO-8859-15");
        final Charset CP1252 = Charset.forName("CP1252");
        FileObject java = sources.createData("a.java");
        Charset enc = FileEncodingQuery.getEncoding(java);
        assertEquals(UTF8,enc);
        FileObject xml = sources.createData("b.xml");
        enc = FileEncodingQuery.getEncoding(xml);
        assertEquals(ISO15,enc);
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(J2SEProjectProperties.SOURCE_ENCODING, CP1252.name());
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            }
        });
        enc = FileEncodingQuery.getEncoding(java);
        assertEquals(CP1252,enc);
        FileObject standAloneJava = scratch.createData("b.java");
        enc = FileEncodingQuery.getEncoding(standAloneJava);
        assertEquals(Charset.defaultCharset(), enc);
    }

    public static class DummyXMLEncodingImpl extends FileEncodingQueryImplementation {

        public Charset getEncoding(FileObject file) {
            if ("xml".equals(file.getExt())) {
                return Charset.forName("ISO-8859-15");
            }
            else {
                return null;
            }
        }
    }

}
