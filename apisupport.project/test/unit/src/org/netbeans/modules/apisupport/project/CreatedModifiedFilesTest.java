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

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import junit.framework.TestCase;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles.Operation;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.modules.SpecificationVersion;

/**
 * Tests {@link CreatedModifiedFiles}.
 * @author Martin Krauskopf
 */
public class CreatedModifiedFilesTest extends LayerTestBase {

    private static final String[] HTML_CONTENT = {
        "<html>",
        "i am some ${file}",
        "</html>"
    };
    
    private static final Map<String,String> TOKENS_MAP = new HashMap<String,String>();
    
    static {
        TOKENS_MAP.put("file", "template");
    }
    
    private static final String[] HTML_CONTENT_TOKENIZED = {
        "<html>",
        "i am some template",
        "</html>"
    };
    
    public CreatedModifiedFilesTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }
    
    public void testCreatedModifiedFiles() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        cmf.add(cmf.bundleKeyDefaultBundle(LocalizedBundleInfo.NAME, "Much Better Name"));
        cmf.add(cmf.bundleKey("src/custom.properties", "some.property", "some value"));
        cmf.add(cmf.addLoaderSection("org/example/module1/MyExtLoader", null));
        cmf.add(cmf.createFile("src/org/example/module1/resources/template.html", createFile(HTML_CONTENT)));
        cmf.add(cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl", false));
        
        assertRelativePaths(
                new String[] {"src/META-INF/services/org.example.spi.somemodule.ProvideMe", "src/custom.properties", "src/org/example/module1/resources/template.html"},
                cmf.getCreatedPaths());
        assertRelativePaths(
                new String[] {"manifest.mf", "src/org/example/module1/resources/Bundle.properties"},
                cmf.getModifiedPaths());

        cmf.run();
    }
    
    public void testBundleKeyDefaultBundle() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        ProjectInformation pi = ProjectUtils.getInformation(project);
        assertEquals("display name before from bundle", "Testing Module", pi.getDisplayName());
        assertEquals("display name before from project", "Testing Module", project.getBundleInfo().getDisplayName());
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.bundleKeyDefaultBundle(LocalizedBundleInfo.NAME, "Much Better Name");
        assertRelativePath("src/org/example/module1/resources/Bundle.properties",
                op.getModifiedPaths());
        op.run();
        
        pi = ProjectUtils.getInformation(project);
        assertEquals("display name after from bundle", "Much Better Name", pi.getDisplayName());
        assertEquals("display name after from project", "Much Better Name", project.getBundleInfo().getDisplayName());
    }
    
    public void testBundleKey() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.bundleKey("src/custom.properties", "some.property", "some value");
        
        assertRelativePath("src/custom.properties", op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(TestBase.file(getWorkDir(), "module1/src/custom.properties")));
        assertEquals("property created", "some value", ep.getProperty("some.property"));
    }
    
    public void testAddLoaderSection() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.addLoaderSection("org/example/module1/MyExtLoader", null);
        
        assertRelativePath("manifest.mf", op.getModifiedPaths());
        
        op.run();
        
        EditableManifest em = Util.loadManifest(FileUtil.toFileObject(TestBase.file(getWorkDir(), "module1/manifest.mf")));
        assertEquals("loader section was added", "Loader", em.getAttribute("OpenIDE-Module-Class", "org/example/module1/MyExtLoader.class"));
    }
    
    public void testAddLookupRegistration() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        cmf.add(cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl1", false));
        cmf.add(cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl2", false));
        cmf.add(cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl1", true));

        String[] paths = {
            "src/META-INF/services/org.example.spi.somemodule.ProvideMe",
            "test/unit/src/META-INF/services/org.example.spi.somemodule.ProvideMe",
        };
        assertRelativePaths(paths, cmf.getCreatedPaths());
        
        cmf.run();

        FileObject registry = project.getProjectDirectory().getFileObject(paths[0]);
        assertNotNull(registry);
        InputStream is = registry.getInputStream();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            assertEquals("org.example.module1.ProvideMeImpl1", r.readLine());
            assertEquals("org.example.module1.ProvideMeImpl2", r.readLine());
            assertEquals(null, r.readLine());
        } finally {
            is.close();
        }
        registry = project.getProjectDirectory().getFileObject(paths[1]);
        assertNotNull(registry);
        is = registry.getInputStream();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            assertEquals("org.example.module1.ProvideMeImpl1", r.readLine());
            assertEquals(null, r.readLine());
        } finally {
            is.close();
        }
    }
    
    public void testCreateFile() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/template.html";
        Operation op = cmf.createFile(templatePath, createFile(HTML_CONTENT));
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(HTML_CONTENT, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testCreateBinaryFile() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/binarytemplate.zip";
        
        FileObject binaryFile = createBinaryFile(HTML_CONTENT);
        
        Operation op = cmf.createFile(templatePath, binaryFile);
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(binaryFile, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testCreateFileWithSubstitutions() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/template.html";
        Operation op = cmf.createFileWithSubstitutions(templatePath, createFile(HTML_CONTENT), TOKENS_MAP);
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(HTML_CONTENT_TOKENIZED, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testAddModuleDependency() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        Operation op = cmf.addModuleDependency("org.apache.tools.ant.module", "3",
                new SpecificationVersion("3.9"), true);
        
        assertRelativePath("nbproject/project.xml", op.getModifiedPaths());
        
        cmf.add(op);
        cmf.run();
        
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        Set<ModuleDependency> deps = pxm.getDirectDependencies();
        assertEquals("one dependency", 1, deps.size());
        ModuleDependency antDep = deps.iterator().next();
        assertEquals("cnb", "org.apache.tools.ant.module", antDep.getModuleEntry().getCodeNameBase());
        assertEquals("release version", "3", antDep.getReleaseVersion());
        assertEquals("specification version", "3.9", antDep.getSpecificationVersion());
        assertTrue("compile dependeny", antDep.hasCompileDependency());
        assertFalse("implementation dependeny", antDep.hasImplementationDepedendency());
    }
    
    public void testTheSameModuleDependencyTwice() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        Operation op = cmf.addModuleDependency("org.apache.tools.ant.module",null,null,false);
        
        assertRelativePath("nbproject/project.xml", op.getModifiedPaths());
        
        cmf.add(op);
        cmf.add(op);
        cmf.run();
        
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        Set deps = pxm.getDirectDependencies();
        assertEquals("one dependency", 1, deps.size());
        ModuleDependency antDep = (ModuleDependency) deps.toArray()[0];
        assertEquals("cnb", "org.apache.tools.ant.module", antDep.getModuleEntry().getCodeNameBase());
    }
    
    public void testCreateLayerEntry() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation layerOp = cmf.createLayerEntry(
                "Menu/Tools/org-example-module1-BeepAction.instance",
                null,
                null,
                null, 
                Collections.<String,Object>singletonMap("position", 400));
        layerOp.run();
        
        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-Module1UI.settings",
                null,
                null,
                null,
                null);
        cmf.add(layerOp);
        assertRelativePath("src/org/example/module1/resources/layer.xml", layerOp.getModifiedPaths());
        
        layerOp = cmf.createLayerEntry(
                "Menu/Tools/org-example-module1-BlareAction.instance",
                null,
                null,
                null, 
                null);
        cmf.add(layerOp);
        
        layerOp = cmf.createLayerEntry(
                "Menu/Tools/org-example-module1-DrumAction.instance",
                null,
                null,
                null, 
                Collections.<String,Object>singletonMap("position", 405));
        cmf.add(layerOp);
        
        layerOp = cmf.orderLayerEntry("Menu/Tools",
                "org-example-module1-BeepAction.instance",
                "org-example-module1-BlareAction.instance",
                "org-example-module1-DrumAction.instance");
        cmf.add(layerOp);
        
        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-Other.settings",
                createFile(HTML_CONTENT),
                null,
                null, 
                null);
        cmf.add(layerOp);

        layerOp = cmf.orderLayerEntry("Services",
                null,
                "org-example-module1-Other.settings",
                "org-example-module1-Module1UI.settings");
        cmf.add(layerOp);
        
        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-Tokenized.settings",
                createFile(HTML_CONTENT),
                TOKENS_MAP,
                null,
                null);
        cmf.add(layerOp);

        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-LocalizedAndTokened.settings",
                createFile(HTML_CONTENT),
                TOKENS_MAP,
                "Some Settings",
                null);
        cmf.add(layerOp);

        assertRelativePaths(
                new String[] {"src/org/example/module1/resources/Bundle.properties", "src/org/example/module1/resources/layer.xml"},
                cmf.getModifiedPaths());
        assertRelativePaths(
                new String[] {
                    "src/org/example/module1/resources/org-example-module1-LocalizedAndTokenedSettings.xml",
                    "src/org/example/module1/resources/org-example-module1-OtherSettings.xml",
                    "src/org/example/module1/resources/org-example-module1-TokenizedSettings.xml"
                },
                cmf.getCreatedPaths());
        cmf.run();

        assertFileContent(HTML_CONTENT_TOKENIZED, new File(getWorkDir(), "module1/src/org/example/module1/resources/org-example-module1-TokenizedSettings.xml"));

        // check layer content
        String[] supposedContent = {
            "<filesystem>",
                    "<folder name=\"Menu\">",
                    "<folder name=\"Tools\">",
                    "<file name=\"org-example-module1-BeepAction.instance\">",
                    "<attr name=\"position\" intvalue=\"400\"/>",
                    "</file>",
                    "<file name=\"org-example-module1-BlareAction.instance\">",
                    "<attr name=\"position\" intvalue=\"402\"/>",
                    "</file>",
                    "<file name=\"org-example-module1-DrumAction.instance\">",
                    "<attr name=\"position\" intvalue=\"405\"/>",
                    "</file>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Services\">",
                    "<attr name=\"org-example-module1-Other.settings/org-example-module1-Module1UI.settings\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-LocalizedAndTokened.settings\" url=\"org-example-module1-LocalizedAndTokenedSettings.xml\">",
                    "<attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"org.example.module1.resources.Bundle\"/>",
                    "</file>",
                    "<file name=\"org-example-module1-Module1UI.settings\"/>",
                    "<file name=\"org-example-module1-Other.settings\" url=\"org-example-module1-OtherSettings.xml\"/>",
                    "<file name=\"org-example-module1-Tokenized.settings\" url=\"org-example-module1-TokenizedSettings.xml\"/>",
                    "</folder>",
                    "</filesystem>"
        };
        assertLayerContent(supposedContent, 
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
        
        // check bundle content
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(
                TestBase.file(getWorkDir(), "module1/src/org/example/module1/resources/Bundle.properties")));
        assertEquals("localized name property", "Some Settings",
                ep.getProperty("Services/org-example-module1-LocalizedAndTokened.settings"));
        assertEquals("module name", "Testing Module", ep.getProperty("OpenIDE-Module-Name"));
    }
    
    public void testCreateLayerAttribute() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        String fqClassName = "org.example.module1.BeepAction";
        String dashedFqClassName = fqClassName.replace('.', '-');
        String layerPath = "Actions/Tools/" + dashedFqClassName + ".instance";
        
        Operation op = cmf.createLayerEntry(layerPath, null, null, null, null);
        cmf.add(op);
        
        op = cmf.createLayerAttribute(
                layerPath, "instanceClass", fqClassName);
        assertRelativePath("src/org/example/module1/resources/layer.xml", op.getModifiedPaths());
        
        cmf.add(op);
        cmf.run();
        
        String[] supposedContent = {
            "<filesystem>",
            "<folder name=\"Actions\">",
            "<folder name=\"Tools\">",
            "<file name=\"org-example-module1-BeepAction.instance\">",
            "<attr name=\"instanceClass\" stringvalue=\"org.example.module1.BeepAction\"/>",
            "</file>",
            "</folder>",
            "</folder>",
            "</filesystem>"
        };
        assertLayerContent(supposedContent, 
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
    }

    /** @see "#64273" */
    public void testCreateLayerEntryWithoutLocalizingBundle() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        project.getProjectDirectory().getFileObject("src/org/example/module/resources/Bundle.properties").delete();
        FileObject mf = project.getProjectDirectory().getFileObject("manifest.mf");
        EditableManifest m;
        InputStream is = mf.getInputStream();
        try {
            m = new EditableManifest(is);
        } finally {
            is.close();
        }
        m.removeAttribute("OpenIDE-Module-Localizing-Bundle", null);
        FileLock lock = mf.lock();
        try {
            OutputStream os = mf.getOutputStream(lock);
            try {
                m.write(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.createLayerEntry("f", null, null, "F!", null);
        cmf.add(op);
        cmf.run();
        String[] supposedContent = new String[] {
            "<filesystem>",
            "<file name=\"f\"/>",
            "</filesystem>"
        };
        assertLayerContent(supposedContent, 
                new File(getWorkDir(), "module/src/org/example/module/resources/layer.xml"));
    }

    public void testLayerEntryOverlappingFilenames() throws Exception { // #85138
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        FileObject content = FileUtil.createMemoryFileSystem().getRoot().createData("x");
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.createLayerEntry("file", content, null, null, null);
        cmf.add(op);
        assertEquals("[src/org/example/module/resources/layer.xml]", Arrays.toString(op.getModifiedPaths()));
        assertEquals("[src/org/example/module/resources/file]", Arrays.toString(op.getCreatedPaths()));
        assertEquals("[]", Arrays.toString(op.getInvalidPaths()));
        cmf.run();
        assertEquals("[src/org/example/module/resources/layer.xml]", Arrays.toString(cmf.getModifiedPaths()));
        assertEquals("[src/org/example/module/resources/file]", Arrays.toString(cmf.getCreatedPaths()));
        assertEquals("[]", Arrays.toString(cmf.getInvalidPaths()));
        assertNotNull(project.getProjectDirectory().getFileObject("src/org/example/module/resources/file"));
        // #1: cannot add the same layer path twice.
        cmf = new CreatedModifiedFiles(project);
        op = cmf.createLayerEntry("file", content, null, null, null);
        cmf.add(op);
        assertEquals("[]", Arrays.toString(op.getModifiedPaths()));
        assertEquals("[]", Arrays.toString(op.getCreatedPaths()));
        assertEquals("[file]", Arrays.toString(op.getInvalidPaths()));
        // #2: if files of the same basename are added twice, uniquify external file.
        cmf = new CreatedModifiedFiles(project);
        op = cmf.createLayerEntry("dir/file", content, null, null, null);
        cmf.add(op);
        assertEquals("[src/org/example/module/resources/layer.xml]", Arrays.toString(op.getModifiedPaths()));
        assertEquals("[src/org/example/module/resources/file_1]", Arrays.toString(op.getCreatedPaths()));
        assertEquals("[]", Arrays.toString(op.getInvalidPaths()));
        cmf.run();
        assertEquals("[src/org/example/module/resources/layer.xml]", Arrays.toString(cmf.getModifiedPaths()));
        assertEquals("[src/org/example/module/resources/file_1]", Arrays.toString(cmf.getCreatedPaths()));
        assertEquals("[]", Arrays.toString(cmf.getInvalidPaths()));
        assertNotNull(project.getProjectDirectory().getFileObject("src/org/example/module/resources/file_1"));
    }

    public static void assertRelativePath(String expectedPath, String[] paths) {
        TestCase.assertEquals("one path", 1, paths.length);
        TestCase.assertEquals("created, modified paths", expectedPath, paths[0]);
    }
    
    public static void assertRelativePath(String expectedPath, SortedSet<String> paths) {
        String[] s = new String[paths.size()];
        assertRelativePath(expectedPath, paths.toArray(s));
    }
    
    public static void assertRelativePaths(String[] expectedPaths, String[] paths) {
        TestCase.assertEquals("created, modified paths", Arrays.asList(expectedPaths), Arrays.asList(paths));
    }
    
    private FileObject createFile(String[] content) throws IOException {
        File myTemplate = new File(getWorkDir(), "myTemplate.html");
        OutputStream myTemplateOS = new FileOutputStream(myTemplate);
        PrintWriter pw = new PrintWriter(myTemplateOS);
        try {
            for (String line : content) {
                pw.println(line);
            }
        } finally {
            pw.close();
        }
        return FileUtil.toFileObject(myTemplate);
    }
    
    private FileObject createBinaryFile(String[] content) throws IOException {
        StringBuilder b = new StringBuilder();
        for (String section : content) {
            b.append(section);
        }
        return TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "myTemplate.zip", "a/b/c/d.txt:" + b);
    }
    
    private void assertFileContent(String[] content, File file) throws IOException {
        assertTrue("file exist and is a regular file", file.isFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            for (int i = 0; i < content.length; i++) {
                assertEquals("file content", content[i], br.readLine());
            }
            assertNull(br.readLine());
        } finally {
            br.close();
        }
    }
    
    private void assertFileContent(FileObject f1, File f2) throws IOException {
        InputStream is = f1.getInputStream();
        InputStream is2 = new FileInputStream(f2);
        
        try {
            byte[] content = new byte[is.available()];
            is.read(content);
            
            byte[] content2 = new byte[is2.available()];
            is2.read(content2);
            
            assertEquals(content.length, content2.length);
            for (int i = 0; i < content.length; i++) {
                assertEquals("file content", content[i], content2[i]);
            }
        } finally {
            is.close();
            is2.close();
        }
    }
    
    public static void assertLayerContent(final String[] supposedContent,
            final File layerF) throws IOException, FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(layerF));
        List<String> actualContent = new ArrayList<String>();
        boolean fsElementReached = false;
        String line;
        
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!fsElementReached && line.equals(supposedContent[0])) {
                    fsElementReached = true;
                    actualContent.add(line);
                    continue;
                }
                if (fsElementReached) {
                    actualContent.add(line);
                }
            }
        } finally {
            reader.close();
        }
        
        assertEquals("content of layer", Arrays.asList(supposedContent).toString(), actualContent.toString());
    }
    
}
