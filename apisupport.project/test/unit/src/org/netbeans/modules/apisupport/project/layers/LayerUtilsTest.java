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

package org.netbeans.modules.apisupport.project.layers;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.startup.layers.LayerCacheManager;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;

/**
 * Test writing changes to layers.
 * @author Jesse Glick
 */
public class LayerUtilsTest extends LayerTestBase {
    private static final String LAYER_NAME_FMT = "layer-%03d.xml";
    private static final String JAR_NAME_FMT = "module-%03d.jar";
    private static final String ACTION_NAME_FMT = "org-test-Action_%03d_%03d.instance";

    static final int NUM_LAYERS = 500;
    static final int NUM_ACTIONS = 100;
    static final int NUM_ACCESSES = 10000;

    private static File cacheDir;
    
    public LayerUtilsTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }

    FileSystem memoryStoreAndLoad(LayerCacheManager m, List<File> files) throws IOException {
        FileSystem[] fss = new FileSystem[files.size()];
        System.out.println("Storing and loading in-memory cache");

        for (int i = 0; i < files.size(); i++) {
            File xf = files.get(i);
            fss[i] = createCachedFS(m, xf);
        }
        return new MultiFileSystem(fss);
    }

    private FileSystem createCachedFS(LayerCacheManager m, File xf) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.store(null, Collections.singletonList(xf.toURL()), os);
        return m.load(null, ByteBuffer.wrap(os.toByteArray()).order(ByteOrder.LITTLE_ENDIAN));
    }

    void store(LayerCacheManager m, List<File> files) throws IOException {
        cacheDir = new File(this.getWorkDir(), "cache");
        assertFalse(cacheDir.exists());
        assertTrue(cacheDir.mkdir());
        System.out.println("Storing external cache into " + cacheDir);

        List<URL> urll = new ArrayList<URL>(Collections.singletonList((URL) null));
        for (int i = 0; i < files.size(); i++) {
            File xf = files.get(i);
            File cf = new File(cacheDir, xf.getName() + ".ser");
            assertFalse(cf.exists());
            assertTrue(cf.createNewFile());
            OutputStream os = new BufferedOutputStream(new FileOutputStream(cf));
            URL url = xf.getName().endsWith(".jar") ? new URL("jar:" + xf.toURI() + "!/" + LAYER_PATH_IN_JAR) : xf.toURL();
            urll.set(0, url);
            m.store(null, urll, os);
            os.close();
        }
    }

    FileSystem load(LayerCacheManager m) throws Exception {
        assertTrue(cacheDir.exists());
        List<File> files = Arrays.asList(cacheDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".ser");
            }
        }));
        System.out.println("Loading external cache from " + cacheDir + ", " + files.size() + " files");
        Collections.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return - f1.getName().compareTo(f2.getName());
            }
        });
        FileSystem[] fss = new FileSystem[files.size()];
        for (int i = 0; i < files.size(); i++) {
            File cf = files.get(i);
            assertFalse(cf.isDirectory());
            long size = cf.length();
            assertTrue(size > 0);
            ByteBuffer bb = (new FileInputStream(cf)).getChannel().map(MapMode.READ_ONLY, 0, size).order(ByteOrder.LITTLE_ENDIAN);
            fss[i] = m.load(null, bb);
        }
        return new MultiFileSystem(fss);
    }

    public void testXMLMultiFS() throws Exception {
        clearWorkDir();
        // b-layer.xml overrides a-layer.xml now and then:
        List<File> files = new ArrayList<File>(Arrays.asList(
                new File(getDataDir(), "layers/b-layer.xml"),
                new File(getDataDir(), "layers/a-layer.xml")
                ));

        FileSystem xfs0 = new XMLFileSystem(files.get(0).toURL());
        FileSystem xfs1 = new XMLFileSystem(files.get(1).toURL());
        FileSystem mfs = new MultiFileSystem(new FileSystem[] { xfs0, xfs1 });
        assertNotNull(xfs1.findResource("Menu/A Folder"));
        assertNotNull(mfs.findResource("Menu/File"));
        assertNotNull(mfs.findResource("Menu/A Folder"));
        assertNull(mfs.findResource("Menu/A Folder/org-example-a-AAction.shadow"));  // hidden by b-layer
        FileObject mf = mfs.findResource("Actions/File");
        assertEquals(2, mf.getChildren().length);
        FileObject ba = mfs.findResource("Actions/File/org-example-b-BAction.instance");
        assertEquals("BBBBB", ba.getAttribute("displayName"));
        FileObject aa = mfs.findResource("Actions/File/org-example-a-AAction.instance");
        assertEquals("AAAA", aa.getAttribute("displayName"));
    }
    public void testCachedMultiFS() throws Exception {
        clearWorkDir();
        LayerCacheManager m = LayerCacheManager.manager(true);
        // b-layer.xml overrides a-layer.xml now and then:
        List<File> files = new ArrayList<File>(Arrays.asList(
                new File(getDataDir(), "layers/b-layer.xml"),
                new File(getDataDir(), "layers/a-layer.xml")
                ));

        FileSystem cfs = memoryStoreAndLoad(m, files);
        assertNotNull(cfs.findResource("Menu/File"));
        assertNotNull(cfs.findResource("Menu/A Folder"));
        assertNull(cfs.findResource("Menu/A Folder/org-example-a-AAction.shadow"));  // hidden by b-layer
        FileObject mf = cfs.findResource("Actions/File");
        assertEquals(2, mf.getChildren().length);
        FileObject ba = cfs.findResource("Actions/File/org-example-b-BAction.instance");
        assertEquals("BBBBB", ba.getAttribute("displayName"));
        FileObject aa = cfs.findResource("Actions/File/org-example-a-AAction.instance");
        assertEquals("AAAA", aa.getAttribute("displayName"));
    }

    public void testMixedMultiFS() throws Exception {
        clearWorkDir();
        LayerCacheManager m = LayerCacheManager.manager(true);
        // b-layer.xml overrides a-layer.xml now and then:
        File lb = new File(getDataDir(), "layers/b-layer.xml");
        File la = new File(getDataDir(), "layers/a-layer.xml");

        FileSystem cfs = createCachedFS(m, lb);
        FileSystem xfs = new XMLFileSystem(la.toURL());
        FileSystem mfs = new MultiFileSystem(new FileSystem[] { cfs, xfs });
        assertNotNull(mfs.findResource("Menu/File"));
        assertNotNull(mfs.findResource("Menu/A Folder"));
        assertNull(mfs.findResource("Menu/A Folder/org-example-a-AAction.shadow"));  // hidden by b-layer
        FileObject mf = mfs.findResource("Actions/File");
        assertEquals(2, mf.getChildren().length);
        FileObject ba = mfs.findResource("Actions/File/org-example-b-BAction.instance");
        assertEquals("BBBBB", ba.getAttribute("displayName"));
        FileObject aa = mfs.findResource("Actions/File/org-example-a-AAction.instance");
        assertEquals("AAAA", aa.getAttribute("displayName"));
    }

    public void testCLFSJarInitialScanPerformance() throws Exception {
        // some real world attempt, store cache actually on disk
        // and compare with performance with original getEffectiveFileSystem (that is with filtering)
        clearWorkDir();
        List<File> files = generateLayers(NUM_LAYERS, NUM_ACTIONS, true);
        LayerCacheManager m = LayerCacheManager.manager(true);

        long start = System.currentTimeMillis();
        store(m, files);
        long stop = System.currentTimeMillis();
        System.out.println("CLFS Jar initial scan takes " + (stop - start) + " ms");
        assertTrue(new File(getWorkDir(), "cache/" + files.get(0).getName() + ".ser").exists());
        System.out.println("CLFS Jar initial scan performance test finished");
    }

    public void testCLFSInitialScanPerformance() throws Exception {
        // some real world attempt, store cache actually on disk
        // and compare with performance with original getEffectiveFileSystem (that is with filtering)
        clearWorkDir();
        List<File> files = generateLayers(NUM_LAYERS, NUM_ACTIONS, false);
        LayerCacheManager m = LayerCacheManager.manager(true);

        long start = System.currentTimeMillis();
        store(m, files);
        long stop = System.currentTimeMillis();
        System.out.println("CLFS initial scan takes " + (stop - start) + " ms");
        assertTrue(new File(getWorkDir(), "cache/" + String.format(LAYER_NAME_FMT, 0) + ".ser").exists());
        System.out.println("CLFS initial scan performance test finished");
    }

    public void testCLFSPerformance() throws Exception {
        LayerCacheManager m = LayerCacheManager.manager(true);
        long start = System.currentTimeMillis();
        FileSystem cfs = load(m);
        long stop = System.currentTimeMillis();
        System.out.println("CLFS cache load takes " + (stop - start) + " ms");

        // 2-nd action of 1-st layer
        assertNotNull(cfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 1, 0)));
        // last action of last layer
        assertNotNull(cfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, NUM_ACTIONS - 1, NUM_LAYERS - 1)));
        // nothing beyond that
        assertNull(cfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, NUM_ACTIONS, NUM_LAYERS - 1)));
        // 1-st action of 1-st layer is hidden
        assertNull(cfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, 0)));
        // last hidden action
        assertNull(cfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, NUM_LAYERS - 2)));
        assertNotNull(cfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, NUM_LAYERS - 1)));

        System.out.println("CLFS " + NUM_ACCESSES + " accesses takes " + measureAccesses(cfs) + " ms");
        System.out.println("CLFS performance test finished");
    }

    private long measureAccesses(FileSystem fs) {
        Random rnd = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_ACCESSES; i++) {
            fs.findResource(String.format(ACTION_NAME_FMT, rnd.nextInt(NUM_ACTIONS), rnd.nextInt(NUM_LAYERS)));
        }
        long stop = System.currentTimeMillis();
        return stop - start;
    }

    public void testXMLFSPerformance() throws Exception {
        clearWorkDir();
        List<File> files = generateLayers(NUM_LAYERS, NUM_ACTIONS, false);

        List<URL> urls = new ArrayList<URL>(NUM_LAYERS);
        for (File f : files) {
            urls.add(f.toURL());
        }
        XMLFileSystem[] xfss = new XMLFileSystem[NUM_LAYERS];

        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_LAYERS; i++) {
            xfss[i] = new XMLFileSystem(urls.get(i));
        }
        FileSystem mfs = new MultiFileSystem(xfss);
        long stop = System.currentTimeMillis();
        System.out.println("XMLFS scan takes " + (stop - start) + " ms");

        // 2-nd action of 1-st layer
        assertNotNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 1, 0)));
        // last action of last layer
        assertNotNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, NUM_ACTIONS - 1, NUM_LAYERS - 1)));
        // nothing beyond that
        assertNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, NUM_ACTIONS, NUM_LAYERS - 1)));
        // 1-st action of 1-st layer is hidden
        assertNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, 0)));
        // last hidden action
        assertNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, NUM_LAYERS - 2)));
        assertNotNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, NUM_LAYERS - 1)));

        System.out.println("SMLFS " + NUM_ACCESSES + " accesses takes " + measureAccesses(mfs) + " ms");
        System.out.println("XMLFS performance test finished");
    }

    public void testXMLFSJarPerformance() throws Exception {
        clearWorkDir();
        List<File> files = generateLayers(NUM_LAYERS, NUM_ACTIONS, true);

        List<URL> urls = new ArrayList<URL>(NUM_LAYERS);
        for (File f : files) {
            urls.add(new URL("jar:" + f.toURI() + "!/" + LAYER_PATH_IN_JAR));
        }
        XMLFileSystem[] xfss = new XMLFileSystem[NUM_LAYERS];

        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_LAYERS; i++) {
            xfss[i] = new XMLFileSystem(urls.get(i));
        }
        FileSystem mfs = new MultiFileSystem(xfss);
        long stop = System.currentTimeMillis();
        System.out.println("XMLFS Jar scan takes " + (stop - start) + " ms");

        // 2-nd action of 1-st layer
        assertNotNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 1, 0)));
        // last action of last layer
        assertNotNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, NUM_ACTIONS - 1, NUM_LAYERS - 1)));
        // nothing beyond that
        assertNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, NUM_ACTIONS, NUM_LAYERS - 1)));
        // 1-st action of 1-st layer is hidden
        assertNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, 0)));
        // last hidden action
        assertNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, NUM_LAYERS - 2)));
        assertNotNull(mfs.findResource("Actions/Test/" + String.format(ACTION_NAME_FMT, 0, NUM_LAYERS - 1)));

        System.out.println("XMLFS Jar performance test finished");
    }

    private static final String LAYER_PATH_IN_JAR = "org/test/layer.xml";

    private List<File> generateLayers(int numLayers, int numActions, boolean jars) throws IOException {
        File wd = getWorkDir();
        List<File> files = new ArrayList<File>(NUM_LAYERS);
        for (int i = 0; i < numLayers; i++) {
            String layerName = String.format(jars ? JAR_NAME_FMT : LAYER_NAME_FMT, i);
            File lf = new File(wd, layerName);
            files.add(lf);
            Writer w;
            if (jars) {
                Manifest man = new Manifest();
                Attributes attr = man.getMainAttributes();
                attr.put(new Attributes.Name("Manifest-Version"), "1.0");
                attr.put(new Attributes.Name(ManifestManager.OPENIDE_MODULE_LAYER), LAYER_PATH_IN_JAR);
                JarOutputStream jos = new JarOutputStream(new FileOutputStream(lf), man);
                jos.putNextEntry(new ZipEntry(LAYER_PATH_IN_JAR));
                w = new OutputStreamWriter(jos);
            } else {
                w = new BufferedWriter(new FileWriter(lf));
            }
            PrintWriter pw = new PrintWriter(w);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.2//EN\" \"http://www.netbeans.org/dtds/filesystem-1_2.dtd\">");
            pw.println("<filesystem>");
            pw.println("    <folder name=\"Actions\">");
            pw.println("        <folder name=\"Test\">");

            for (int j = 0; j < numActions; j++) {
                pw.format ("            <file name=\"" + ACTION_NAME_FMT + "\">\n", j, i);
                //pw.println("                <attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"org.netbeans.modules.apisupport.project.layers.TestBundle\"/>");
                pw.format ("                <attr name=\"delegate\" newvalue=\"org.test.Action_%03d_%03d\"/>\n", j, i);
                pw.format ("                <attr name=\"displayName\" bundlevalue=\"org.netbeans.modules.apisupport.project.layers.TestBundle#CTL_ActionMod%d\"/>\n", j % 10);
                pw.println("                <attr name=\"iconBase\" stringvalue=\"org/netbeans/core/startup/layers/icon.png\"/>");
                pw.println("                <attr name=\"instanceCreate\" methodvalue=\"org.openide.awt.Actions.alwaysEnabled\"/>");
                pw.println("                <attr name=\"noIconInMenu\" stringvalue=\"false\"/>");
                pw.println("            </file>");
            }

            // and add first action from previous layer
            if (i > 0)
                pw.format ("            <file name=\"org-test-Action_000_%03d.instance_hidden\"/>\n", i - 1);

            pw.println("        </folder>");
            pw.println("    </folder>");
            pw.println("</filesystem>");
            pw.close();
        }
        Collections.reverse(files);
        return files;
    }

    public void testLayerHandle() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileObject expectedLayerXML = project.getProjectDirectory().getFileObject("src/org/example/module/resources/layer.xml");
        assertNotNull(expectedLayerXML);
        FileObject layerXML = handle.getLayerFile();
        assertNotNull("layer.xml already exists", layerXML);
        assertEquals("right layer file", expectedLayerXML, layerXML);
        FileSystem fs = handle.layer(true);
        assertEquals("initially empty", 0, fs.getRoot().getChildren().length);
        long initialSize = layerXML.getSize();
        fs.getRoot().createData("foo");
        assertEquals("not saved yet", initialSize, layerXML.getSize());
        fs = handle.layer(true);
        assertNotNull("still have in-memory mods", fs.findResource("foo"));
        fs.getRoot().createData("bar");
        handle.save();
        assertTrue("now it is saved", layerXML.getSize() > initialSize);
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"bar\"/>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        assertEquals("right contents too", xml, TestBase.slurp(layerXML));
        // XXX test that nbres: file contents work
    }

    public void testLayerAutoSave() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileSystem fs = handle.layer(true);
        handle.setAutosave(true);
        FileObject foo = fs.getRoot().createData("foo");
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        assertEquals("saved automatically", xml, TestBase.slurp(layerXML));
        foo.setAttribute("a", Boolean.TRUE);
        xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\">\n" +
                "        <attr name=\"a\" boolvalue=\"true\"/>\n" +
                "    </file>\n" +
                "</filesystem>\n";
        assertEquals("saved automatically from an attribute change too", xml, TestBase.slurp(layerXML));
    }

    // XXX testInitiallyInvalidLayer
    // XXX testInitiallyMissingLayer
    // XXX testGcLayerHandle

    public void testSystemFilesystemStandaloneProject() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        TestBase.dump(layerXML, xml);
        long start = System.currentTimeMillis();
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        System.err.println("LayerUtils.getEffectiveSystemFilesystem ran in " + (System.currentTimeMillis() - start) + "msec");
        assertFalse("can write to it", fs.isReadOnly());
        assertNotNull("have stuff from the platform", fs.findResource("Menu/File"));
        assertNotNull("have stuff from my own layer", fs.findResource("foo"));
        fs.getRoot().createData("quux");
        handle.save();
        xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "    <file name=\"quux\"/>\n" +
                "</filesystem>\n";
        assertEquals("new layer stored", xml, TestBase.slurp(layerXML));
    }

    public void testSystemFilesystemSuiteComponentProject() throws Exception {
        File suiteDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT, false);
        File module1Dir = new File(suiteDir, "testModule1");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module1Dir,
                "test.module1",
                "module1",
                "test/module1/resources/Bundle.properties",
                "test/module1/resources/layer.xml",
                suiteDir);
        NbModuleProject module1 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module1Dir));
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(module1);
        FileUtil.createData(handle.layer(true).getRoot(), "random/stuff");
        handle.save();
        File module2Dir = new File(suiteDir, "testModule2");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module2Dir,
                "test.module2",
                "module2",
                "test/module2/resources/Bundle.properties",
                "test/module2/resources/layer.xml",
                suiteDir);
        NbModuleProject module2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module2Dir));
        handle = LayerUtils.layerForProject(module2);
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"existing\"/>\n" +
                "</filesystem>\n";
        TestBase.dump(layerXML, xml);
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(module2);
        assertFalse("can write to it", fs.isReadOnly());
        assertNotNull("have stuff from the platform", fs.findResource("Menu/File"));
        assertNotNull("have stuff from my own layer", fs.findResource("existing"));
        assertNotNull("have stuff from other modules in the same suite", fs.findResource("random/stuff"));
        fs.getRoot().createData("new");
        handle.save();
        xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"existing\"/>\n" +
                "    <file name=\"new\"/>\n" +
                "</filesystem>\n";
        assertEquals("new layer stored", xml, TestBase.slurp(layerXML));
    }

    public void testSystemFilesystemLocalizedNames() throws Exception {
        File suiteDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT, false);
        File module1Dir = new File(suiteDir, "testModule1");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module1Dir,
                "test.module1",
                "module1",
                "test/module1/resources/Bundle.properties",
                "test/module1/resources/layer.xml",
                suiteDir);
        NbModuleProject module1 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module1Dir));
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(module1);
        cmf.add(cmf.createLayerEntry("foo", null, null, "Foo", null));
        cmf.run();
        File module2Dir = new File(suiteDir, "testModule2");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module2Dir,
                "test.module2",
                "module2",
                "test/module2/resources/Bundle.properties",
                "test/module2/resources/layer.xml",
                suiteDir);
        NbModuleProject module2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module2Dir));
        cmf = new CreatedModifiedFiles(module2);
        cmf.add(cmf.createLayerEntry("bar", null, null, "Bar", null));
        cmf.add(cmf.createLayerEntry("test-module2-MyAction.instance", null, null, null, null));
        cmf.add(cmf.createLayerEntry("test-module2-some-action.instance", null, null, null, Collections.<String,Object>singletonMap("instanceClass", "test.module2.SomeAction")));
        cmf.add(cmf.createLayerEntry("test-module2-another-action.instance", null, null, null, Collections.<String,Object>singletonMap("instanceCreate", "newvalue:test.module2.AnotherAction")));
        cmf.add(cmf.createLayerEntry("test-module2-factory-action.instance", null, null, null, Collections.<String,Object>singletonMap("instanceCreate", "methodvalue:test.module2.FactoryAction.create")));
        cmf.add(cmf.createLayerEntry("test-module2-localized-action.instance", null, null, "Localized Action", Collections.<String,Object>singletonMap("instanceCreate", "methodvalue:test.module2.LocalizedAction.create")));
        cmf.add(cmf.createLayerEntry("sep-42.instance", null, null, null, Collections.<String,Object>singletonMap("instanceClass", "javax.swing.JSeparator")));
        cmf.add(cmf.createLayerEntry("link-to-standard.shadow", null, null, null, Collections.<String,Object>singletonMap("originalFile", "Actions/System/org-openide-actions-OpenAction.instance")));
        cmf.add(cmf.createLayerEntry("link-to-custom.shadow", null, null, null, Collections.<String,Object>singletonMap("originalFile", "test-module2-MyAction.instance")));
        cmf.add(cmf.createLayerEntry("link-to-localized.shadow", null, null, null, Collections.<String,Object>singletonMap("originalFile", "test-module2-localized-action.instance")));
        File dummyDir = new File(getWorkDir(), "dummy");
        dummyDir.mkdir();
        cmf.add(cmf.createLayerEntry("link-to-url.shadow", null, null, null, Collections.<String,Object>singletonMap("originalFile", dummyDir.toURI().toURL())));
        cmf.run();
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(module2);
//    XXX: failing test, fix or delete
//        assertDisplayName(fs, "right display name for platform file", "Menu/RunProject", "Run");
        assertDisplayName(fs, "label for file in suite", "foo", "Foo");
        assertDisplayName(fs, "label for file in this project", "bar", "Bar");
        assertDisplayName(fs, "right display name for well-known action", "Menu/File/org-openide-actions-SaveAction.instance", "Save");
        assertDisplayName(fs, "label for simple instance", "test-module2-MyAction.instance", "<instance of MyAction>");
        assertDisplayName(fs, "label for instanceClass", "test-module2-some-action.instance", "<instance of SomeAction>");
        assertDisplayName(fs, "label for newvalue instanceCreate", "test-module2-another-action.instance", "<instance of AnotherAction>");
        assertDisplayName(fs, "label for methodvalue instanceCreate", "test-module2-factory-action.instance", "<instance from FactoryAction.create>");
        assertDisplayName(fs, "label for localized methodvalue instanceCreate", "test-module2-localized-action.instance", "Localized Action");
        assertDisplayName(fs, "label for menu separator", "sep-42.instance", "<separator>");
        assertDisplayName(fs, "link to standard menu item", "link-to-standard.shadow", "Open");
        assertDisplayName(fs, "link to custom menu item", "link-to-custom.shadow", "<instance of MyAction>");
        assertDisplayName(fs, "link to localized action", "link-to-localized.shadow", "Localized Action");
        DataObject.find(fs.findResource("link-to-url.shadow")).getNodeDelegate().getDisplayName(); // #65665
//        XXX too hard to unit test in practice, since we will get a CNFE trying to load a class from editor here:
//        //System.err.println("items in Menu/Edit: " + java.util.Arrays.asList(fs.findResource("Menu/Edit").getChildren()));
//        assertDisplayName(fs, "right display name for non-action with only menu presenter", "Menu/Edit/org-netbeans-modules-editor-MainMenuAction$FindSelectionAction.instance", "Find Selection");
    }

    public void testSystemFilesystemLocalizedNamesI18N() throws Exception {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(Locale.JAPAN);
            File platformDir = new File(getWorkDir(), "testPlatform");
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "platform.module");
            mf.getMainAttributes().putValue("OpenIDE-Module-Layer", "platform/module/layer.xml");
            Map<String,String> contents = new HashMap<String,String>();
            contents.put("platform/module/Bundle.properties", "folder/file=English");
            contents.put("platform/module/layer.xml", "<filesystem><folder name=\"folder\"><file name=\"file\"><attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"platform.module.Bundle\"/></file></folder></filesystem>");
            TestBase.createJar(new File(platformDir, "cluster/modules/platform-module.jar".replace('/', File.separatorChar)), contents, mf);
            assertTrue("cluster/config/Modules folder in platform, so that cluster is considered valid.", (new File(platformDir, "cluster/config/Modules")).mkdirs());
            mf = new Manifest();
            contents = new HashMap<String,String>();
            contents.put("platform/module/Bundle_ja.properties", "folder/file=Japanese");
            TestBase.createJar(new File(platformDir, "cluster/modules/locale/platform-module_ja.jar".replace('/', File.separatorChar)), contents, mf);
            // To satisfy NbPlatform.isValid:
            TestBase.createJar(new File(new File(new File(platformDir, "platform"), "core"), "core.jar"), Collections.EMPTY_MAP, new Manifest());
            NbPlatform.addPlatform("testplatform", platformDir, "Test Platform");
            File suiteDir = new File(getWorkDir(), "testSuite");
            SuiteProjectGenerator.createSuiteProject(suiteDir, "testplatform", false);
            File moduleDir = new File(suiteDir, "testModule");
            NbModuleProjectGenerator.createSuiteComponentModule(
                    moduleDir,
                    "test.module",
                    "module",
                    "test/module/resources/Bundle.properties",
                    "test/module/resources/layer.xml",
                    suiteDir);
            NbModuleProject module = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(moduleDir));
            FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(module);
            assertDisplayName(fs, "#64779: localized platform filename", "folder/file", "Japanese");
        } finally {
            Locale.setDefault(orig);
        }
    }

    public void testLocalizedXMLFS() throws Exception {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(Locale.JAPAN);
        } finally {
            Locale.setDefault(orig);
        }
    }

    public void testSystemFilesystemNetBeansOrgProject() throws Exception {
        FileObject nbroot = FileUtil.toFileObject(new File(System.getProperty("test.nbroot")));
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(nbroot.getFileObject("image"));
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(p);
//    XXX: failing test, fix or delete
//        assertDisplayName(fs, "right display name for netbeans.org standard file", "Menu/RunProject", "Run");
        assertNull("not loading files from extra modules", fs.findResource("Templates/Documents/docbook-article.xml"));
        FileObject docbook = nbroot.getFileObject("contrib/docbook");
        if (docbook == null) {
            System.err.println("Skipping part of testSystemFilesystemNetBeansOrgProject since contrib is not checked out");
            return;
        }
        p = (NbModuleProject) ProjectManager.getDefault().findProject(docbook);
        fs = LayerUtils.getEffectiveSystemFilesystem(p);
        assertDisplayName(fs, "right display name for file from extra module", "Templates/Documents/docbook-article.xml", "DocBook Article");
    }

    // XXX testClusterAndModuleExclusions
    // XXX testSystemFilesystemSuiteProject

    private static void assertDisplayName(FileSystem fs, String message, String path, String label) throws Exception {
        FileObject file = fs.findResource(path);
        assertNotNull("found " + path, file);
        assertEquals(message, label, DataObject.find(file).getNodeDelegate().getDisplayName());
    }

    public void testMasks() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        Set<String> optionInstanceNames = new HashSet<String>();
        FileObject toolsMenu = fs.findResource("Menu/Tools");
        assertNotNull(toolsMenu);
        for (FileObject kid : toolsMenu.getChildren()) {
            String name = kid.getNameExt();
            if (name.indexOf("Options") != -1) {
                optionInstanceNames.add(name);
            }
        }
        assertEquals("#63295: masks work",
                new HashSet<String>(Arrays.asList(
            "org-netbeans-modules-options-OptionsWindowAction.shadow"
            // org-netbeans-core-actions-OptionsAction.instance should be masked
        )), optionInstanceNames);
        assertNotNull("system FS has xml/catalog", fs.findResource("Services/Hidden/CatalogProvider/org-netbeans-modules-xml-catalog-impl-XCatalogProvider.instance"));
        assertNull("but one entry hidden by apisupport/project", fs.findResource("Services/Hidden/org-netbeans-modules-xml-catalog-impl-SystemCatalogProvider.instance"));
    }
}
