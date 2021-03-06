/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ant;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.NbCollections;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProjectLibraryProviderTest extends NbTestCase {

    public ProjectLibraryProviderTest(String name) {
        super(name);
    }

    private FileObject projdir;
    private AntProjectHelper helper;
    private Project project;
    private URL base;
    private TestLibraryProvider libraryProvider;
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        libraryProvider = new TestLibraryProvider();
        MockLookup.setLookup(Lookups.fixed(AntBasedTestUtil.testAntBasedProjectType(), AntBasedTestUtil.testCollocationQueryImplementation(getWorkDir()), libraryProvider),
                // Filter out standard CQIs since they are bogus.
                Lookups.exclude(Lookups.metaInfServices(ProjectLibraryProviderTest.class.getClassLoader()), CollocationQueryImplementation.class));
        projdir = TestUtil.makeScratchDir(this).createFolder("prj");
        helper = ProjectGenerator.createProject(projdir, "test");
        project = ProjectManager.getDefault().findProject(projdir);
        close(OpenProjects.getDefault().getOpenProjects());
        base = getWorkDir().toURI().toURL();
        ProjectLibraryProvider.FIRE_CHANGES_SYNCH = true;
        registerTestLibraryTypeProvider();
    }
    
    
    public void testPatternMatching() throws Exception {
        Matcher matcher = ProjectLibraryProvider.LIBS_LINE.matcher("libs.grapht.classpath");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(2), "classpath");
        matcher = ProjectLibraryProvider.LIBS_LINE.matcher("libs.grapht_1_0.classpath");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(2), "classpath");
        matcher = ProjectLibraryProvider.LIBS_LINE.matcher("libs.grapht_1_0.classpath2");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(2), "classpath2");
        matcher = ProjectLibraryProvider.LIBS_LINE.matcher("libs.grapht_1_0.classpath_1");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(2), "classpath_1");
        matcher = ProjectLibraryProvider.LIBS_LINE.matcher("libs.grapht-1.0.classpath");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(2), "classpath");
        matcher = ProjectLibraryProvider.LIBS_LINE.matcher("libs.grapht-1.0.classpath_1");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(2), "classpath_1");
        matcher = ProjectLibraryProvider.LIBS_LINE.matcher("libs.grapht-1.0.1-classpath");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(2), "1-classpath");
    }

    // XXX test name/type/description
    // XXX test : vs. ; and / vs. \ (in <definitions> and in *.properties)
    // XXX test set name, description

    public void testLibraryLoadingBasic() throws Exception {
        writeProperties("libs/my libraries.properties",
                "libs.jgraph.classpath=${base}/jgraph.jar:${base}/../extra libs/jgraph-extras.jar",
                "libs.jgraph.javadoc=${base}/api/jgraph-docs:${base}/api/jgraph-docs.zip!/docs/api/",
                "irrelevant=stuff");
        storeDefs(project, "../libs/my libraries.properties");
        Library lib = LibraryManager.forLocation(new URL(base, "libs/my%20libraries.properties")).getLibrary("jgraph");
        assertNotNull(lib);
        assertEquals("jgraph", lib.getName());
        assertEquals("jgraph", lib.getDisplayName());
        assertNull(lib.getDescription());
        assertEquals("j2se", lib.getType());
        assertEquals(Arrays.asList(new URL("jar:file:jgraph.jar!/"), new URL("jar:file:../extra%20libs/jgraph-extras.jar!/")), lib.getRawContent("classpath"));
        assertEquals(Arrays.asList(new URL("file:api/jgraph-docs/"), new URL("jar:file:api/jgraph-docs.zip!/docs/api/")), lib.getRawContent("javadoc"));
        assertEquals(Collections.emptyList(), lib.getContent("src"));
    }

    public void testLibraryLoadingPrivateAbsolute() throws Exception {
        writeProperties("libs/libraries.properties",
                "libs.jgraph.classpath=${base}/jgraph.jar");
        writeProperties("libs/libraries-private.properties",
                "libs.jgraph.src=" + new File(getWorkDir(), "jgraph-src.zip"),
                "libs.jgraph.javadoc=" + new File(getWorkDir(), "jgraph-api"));
        storeDefs(project, "../libs/libraries.properties");
        Library lib = LibraryManager.forLocation(new URL(base, "libs/libraries.properties")).getLibrary("jgraph");
        assertEquals(Collections.singletonList(new URL("jar:file:jgraph.jar!/")), lib.getRawContent("classpath"));
        assertEquals(Collections.singletonList(new URL("jar:" + base + "jgraph-src.zip!/")), lib.getRawContent("src"));
        assertEquals(Collections.singletonList(new URL(base, "jgraph-api/")), lib.getRawContent("javadoc"));
    }

    public void testPrivateOverridesSharedProperties() throws Exception {
        writeProperties("libs/libraries.properties",
                "libs.jgraph.classpath=${base}/jgraph.jar");
        writeProperties("libs/libraries-private.properties",
                "libs.jgraph.classpath=" + new File(getWorkDir(), "jgraph-api"));
        storeDefs(project, "../libs/libraries.properties");
        Library lib = LibraryManager.forLocation(new URL(base, "libs/libraries.properties")).getLibrary("jgraph");
        assertEquals(Collections.singletonList(new URL(base, "jgraph-api/")), lib.getContent("classpath"));
    }

    public void testSetContent() throws Exception {
        writeProperties("libs/libraries.properties",
                "libs.jgraph.classpath=");
        storeDefs(project, "../libs/libraries.properties");
        Library lib = LibraryManager.forLocation(new URL(base, "libs/libraries.properties")).getLibrary("jgraph");
        setLibraryContent(lib, "classpath", new URL("jar:file:jgraph.jar!/"), new URL("jar:file:../extra%20libs/jgraph-extras.jar!/"));
        setLibraryContent(lib, "src", new URL(base, "separate/jgraph-src/"), new URL(base, "jgraph-other-src/"));
        setLibraryContent(lib, "javadoc", new URL("jar:" + base + "separate/jgraph-api.zip!/"), new URL("jar:file:../separate/jgraph-api.zip!/docs/api/"));
        Map<String,String> m = new HashMap<String,String>();
        File separate = new File(getWorkDir(), "separate");
        m.put("libs.jgraph.classpath", "${base}/jgraph.jar"+File.pathSeparatorChar+"${base}/../extra libs/jgraph-extras.jar");
        m.put("libs.jgraph.src", new File(separate, "jgraph-src").getAbsolutePath().replace('\\', '/') + File.pathSeparator + 
                new File(getWorkDir(), "jgraph-other-src").getAbsolutePath().replace('\\', '/'));
        m.put("libs.jgraph.javadoc", new File(separate, "jgraph-api.zip").getAbsolutePath().replace('\\', '/') + File.pathSeparator + 
                "${base}/../separate/jgraph-api.zip!/docs/api/");
        assertEquals(m, loadProperties("libs/libraries.properties"));
    }

    public void testAreaChangesFromProjectsOpenedClosed() throws Exception {
        storeDefs(project, "../libraries.properties");
        assertEquals("[<none>]", openedLibraryManagers());
        open(project);
        assertEquals("[<none>, " + base + "libraries.properties]", openedLibraryManagers());
        close(project);
        assertEquals("[<none>]", openedLibraryManagers());
        Project project2 = ProjectManager.getDefault().findProject(
                ProjectGenerator.createProject(projdir.getParent().createFolder("prj2"), "test").getProjectDirectory());
        storeDefs(project2, "../lib2.properties");
        open(project, project2);
        assertEquals("[<none>, " + base + "lib2.properties, "+ base + "libraries.properties]", openedLibraryManagers());
        close(project);
        assertEquals("[<none>, " + base + "lib2.properties]", openedLibraryManagers());
    }
    private static String openedLibraryManagers() {
        List<String> urls = new ArrayList<String>();
        for (LibraryManager mgr : LibraryManager.getOpenManagers()) {
            URL u = mgr.getLocation();
            urls.add(u != null ? u.toExternalForm() : "<none>");
        }
        Collections.sort(urls);
        return urls.toString();
    }

    public void testChangesLibraries() throws Exception {
        PropertyProvider pp = helper.getProjectLibrariesPropertyProvider();
        writeProperties("libraries.properties",
                "libs.jgraph.classpath=");
        storeDefs(project, "../libraries.properties");
        LibraryManager mgr = LibraryManager.forLocation(new URL(base, "libraries.properties"));
        Library lib1 = mgr.getLibrary("jgraph");
        assertEquals(Collections.emptyList(), lib1.getContent("classpath"));
        assertEquals("{libs.jgraph.classpath=}", new TreeMap<String,String>(pp.getProperties()).toString());
        MockPropertyChangeListener liblist = new MockPropertyChangeListener(LibraryManager.PROP_LIBRARIES);
        MockPropertyChangeListener contentlist = new MockPropertyChangeListener(Library.PROP_CONTENT);
        mgr.addPropertyChangeListener(liblist);
        lib1.addPropertyChangeListener(contentlist);
        MockChangeListener pplist = new MockChangeListener();
        pp.addChangeListener(pplist);
        writeProperties("libraries.properties",
                "libs.jgraph.classpath=${base}/jgraph",
                "libs.collections.classpath=${base}/collections");
        contentlist.assertEventCount(1);
        assertEquals(Collections.singletonList(new URL("file:jgraph/")), lib1.getRawContent("classpath"));
        liblist.assertEventCount(1);
        assertEquals(lib1, mgr.getLibrary("jgraph"));
        Library lib2 = mgr.getLibrary("collections");
        assertEquals(Collections.singletonList(new URL("file:collections/")), lib2.getRawContent("classpath"));
        pplist.assertEventCount(1);
        assertEquals(("{libs.collections.classpath=" + getWorkDir() + "/collections, libs.jgraph.classpath=" + 
                getWorkDir() + "/jgraph}").replace('/', File.separatorChar),
                new TreeMap<String,String>(pp.getProperties()).toString());
        writeProperties("others.properties",
                "libs.jrcs.classpath=");
        storeDefs(project, "../others.properties");
        contentlist.assertEventCount(0);
        liblist.assertEventCount(0);
        // storeDefs() fires configurationXmlChanged twice - after put() and after save()
        pplist.assertEventCount(2);
        assertEquals(("{libs.jrcs.classpath=}").replace('/', File.separatorChar),
                new TreeMap<String,String>(pp.getProperties()).toString());
    }

    public void testCreateRemoveLibrary() throws Exception {
        LibraryManager mgr = LibraryManager.forLocation(new URL(base, "libraries.properties"));
        Map<String,List<URL>> content = new HashMap<String,List<URL>>();
        content.put("classpath", Arrays.asList(new URL("jar:file:jh.jar!/"), new URL("jar:file:jh-search.jar!/")));
        content.put("javadoc", Arrays.asList(new URL("file:jh-api/")));
        Library lib = mgr.createLibrary("j2se", "javahelp", content);
        assertEquals("j2se", lib.getType());
        assertEquals("javahelp", lib.getName());
        assertEquals(content.get("classpath"), lib.getRawContent("classpath"));
        assertEquals(content.get("javadoc"), lib.getRawContent("javadoc"));
        lib = mgr.createLibrary("j2me", "gps", Collections.<String,List<URL>>emptyMap());
        assertEquals("j2me", lib.getType());
        assertEquals("gps", lib.getName());
        Map<String,String> expected = new HashMap<String,String>();
        expected.put("libs.javahelp.classpath", "${base}/jh.jar"+File.pathSeparatorChar+"${base}/jh-search.jar");
        expected.put("libs.javahelp.javadoc", "${base}/jh-api");
        expected.put("libs.gps.type", "j2me");
        assertEquals(expected, loadProperties("libraries.properties"));
        mgr.removeLibrary(lib);
        expected.remove("libs.gps.type");
        assertEquals(expected, loadProperties("libraries.properties"));
    }

    public void testPropertyProviderBasic() throws Exception {
        writeProperties("libs/libraries.properties",
                "libs.jgraph.classpath=${base}/jgraph.jar:${base}/../extralibs/jgraph-extras.jar");
        storeDefs(project, "../libs/libraries.properties");
        PropertyProvider pp = helper.getProjectLibrariesPropertyProvider();
        assertEquals(Collections.singletonMap("libs.jgraph.classpath", (getWorkDir() + "/libs/jgraph.jar:" + 
            getWorkDir() + "/libs/../extralibs/jgraph-extras.jar").replace('/', File.separatorChar)), pp.getProperties());
    }

    public void testPropertyProviderPrivateAbsolute() throws Exception {
        writeProperties("libs/libraries.properties",
                "libs.jgraph.classpath=${base}/jgraph-1.0.jar");
        writeProperties("libs/libraries-private.properties",
                "libs.jgraph.classpath=" + new File(getWorkDir(), "jgraph-2.0-beta.jar"));
        storeDefs(project, "../libs/libraries.properties");
        PropertyProvider pp = helper.getProjectLibrariesPropertyProvider();
        assertEquals(Collections.singletonMap("libs.jgraph.classpath", new File(getWorkDir(), "jgraph-2.0-beta.jar").getAbsolutePath()), pp.getProperties());
    }

    public void testSharability() throws Exception {
        assertSharability(SharabilityQuery.UNKNOWN, "libs/index.properties");
        assertSharability(SharabilityQuery.NOT_SHARABLE, "libs/index-private.properties");
        assertSharability(SharabilityQuery.SHARABLE, "prj/libs/index.properties");
        assertSharability(SharabilityQuery.NOT_SHARABLE, "prj/libs/index-private.properties");
        assertSharability(SharabilityQuery.SHARABLE, "prj/libs/");
        storeDefs(project, "libs/index.properties");
        assertSharability(SharabilityQuery.SHARABLE, "prj/libs/index.properties");
        assertSharability(SharabilityQuery.NOT_SHARABLE, "prj/libs/index-private.properties");
        assertSharability(SharabilityQuery.MIXED, "prj/libs/");
    }
    private void assertSharability(int mode, String path) throws Exception {
        File f = new File(getWorkDir(), path.replace('/', File.separatorChar));
        if (path.endsWith("/")) {
            FileUtil.createFolder(f);
        } else {
            FileUtil.createData(f);
        }
        assertEquals(mode, SharabilityQuery.getSharability(f));
    }

    private void writeProperties(String path, String... properties) throws IOException {
        FileObject f = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), path);
        EditableProperties ep = new EditableProperties();
        for (String def : properties) {
            String[] nameValue = def.split("=", 2);
            ep.put(nameValue[0], nameValue[1]);
        }
        OutputStream os = f.getOutputStream();
        ep.store(os);
        os.close();
    }

    private static void storeDefs(Project project, String... definitions) throws IOException {
        Document doc = XMLUtil.createDocument("x", null, null, null);
        Element libraries = doc.createElementNS("http://www.netbeans.org/ns/ant-project-libraries/1", "libraries");
        for (String def : definitions) {
            libraries.appendChild(doc.createElementNS("http://www.netbeans.org/ns/ant-project-libraries/1", "definitions")).appendChild(doc.createTextNode(def));
        }
        project.getLookup().lookup(AuxiliaryConfiguration.class).putConfigurationFragment(libraries, true);
        ProjectManager.getDefault().saveProject(project); // to assist in debugging
    }

    private static void open(Project... projects) {
        OpenProjects.getDefault().open(projects, false);
    }

    private static void close(Project... projects) {
        OpenProjects.getDefault().close(projects);
    }

    private static void setLibraryContent(Library lib, String volumeType, URL... paths) throws Exception {
        MockPropertyChangeListener l = new MockPropertyChangeListener(Library.PROP_CONTENT);
        lib.addPropertyChangeListener(l);
        LibraryImplementation impl = getLibraryImplementation(lib);
        List<URL> path = Arrays.asList(paths);
        impl.setContent(volumeType, path);
        l.assertEventCount(1);
        assertEquals(path, lib.getRawContent(volumeType));
    }

    private static LibraryImplementation getLibraryImplementation(Library lib) throws Exception {
        Method getLibraryImplementation = Library.class.getDeclaredMethod("getLibraryImplementation");
        getLibraryImplementation.setAccessible(true);
        return (LibraryImplementation) getLibraryImplementation.invoke(lib);
    }

    private Map<String,String> loadProperties(String path) throws IOException {
        File f = new File(getWorkDir(), path.replace('/', File.separatorChar));
        if (!f.isFile()) {
            return Collections.emptyMap();
        }
        Properties p = new Properties();
        InputStream is = new FileInputStream(f);
        p.load(is);
        is.close();
        return NbCollections.checkedMapByFilter(p, String.class, String.class, true);
    }

    /**
     * Test of copyLibrary method, of class LibrariesSupport.
     */
    public void testCopyLibrary() throws Exception {
        File f = new File(this.getWorkDir(), "bertie.jar");
        createFakeJAR(f, "smth");
        File f1 = new File(this.getWorkDir(), "dog.jar");
        createFakeJAR(f1, "smth");
        new File(this.getWorkDir(), "sources").mkdir();
        File f2 = new File(this.getWorkDir(), "sources/bertie.jar");
        createFakeJAR(f2, "docs/api/test.smth");
        new File(this.getWorkDir(), "libraries").mkdir();
        File f3 = new File(this.getWorkDir(), "libraries/libs.properties");
        f3.createNewFile();
        FileUtil.toFileObject(getWorkDir()).refresh();
        LibraryImplementation l1 = LibrariesSupport.createLibraryImplementation("j2test", new String[]{"jars", "sources"});
        l1.setName("vino");
        l1.setContent("jars", Arrays.asList(new URL[]{f.toURI().toURL(), f1.toURI().toURL()}));
        l1.setContent("sources", Arrays.asList(new URL[]{new URL("jar:" + f2.toURI().toURL() + "!/docs/api/")}));
        libraryProvider.set(l1);
        Library l = LibraryManager.getDefault().getLibrary("vino");
        assertNotNull(l);
        assertEquals(LibraryManager.getDefault(), l.getManager());
        URL u = f3.toURI().toURL();
        Library result = ProjectLibraryProvider.copyLibrary(l, u, false);
        assertNotNull(result);
        assertEquals(u, result.getManager().getLocation());
        assertEquals(Arrays.asList(new URL("jar:file:vino/bertie.jar!/"),
                new URL("jar:file:vino/dog.jar!/")), result.getRawContent("jars"));
        assertEquals(Arrays.asList(new URL("jar:file:vino/bertie-2.jar!/docs/api/")), result.getRawContent("sources"));
        assertEquals("vino", result.getName());
        assertEquals("j2test", result.getType());
        //assertNotNull(LibrariesSupport.resolveLibraryEntryFileObject(u, result.getContent("jars").get(0)));
        assertEquals(new File(this.getWorkDir(), "libraries/vino/bertie.jar").getPath(), 
                FileUtil.toFile(LibrariesSupport.resolveLibraryEntryFileObject(u, FileUtil.getArchiveFile(result.getRawContent("jars").get(0)))).getPath());
        //assertNotNull(LibrariesSupport.resolveLibraryEntryFileObject(u, result.getContent("sources").get(0)));
        assertEquals(new File(this.getWorkDir(), "libraries/vino/bertie-2.jar").getPath(), 
                FileUtil.toFile(LibrariesSupport.resolveLibraryEntryFileObject(u, FileUtil.getArchiveFile(result.getRawContent("sources").get(0)))).getPath());
    }
    
    private void createFakeJAR(File f, String content) throws IOException {
        // create just enough to make URLMapper recognize file as JAR:
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f));
        writeZipFileEntry(zos, content, "some content".getBytes());
        zos.finish();
        zos.close();
    }

    private static void writeZipFileEntry(ZipOutputStream zos, String zipEntryName, byte[] byteArray) throws IOException {
        int byteArraySize = byteArray.length;

        CRC32 crc = new CRC32();
        crc.update(byteArray, 0, byteArraySize);

        ZipEntry entry = new ZipEntry(zipEntryName);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(byteArraySize);
        entry.setCrc(crc.getValue());

        zos.putNextEntry(entry);
        zos.write(byteArray, 0, byteArraySize);
        zos.closeEntry();
    }
    

    public static class TestLibraryProvider implements LibraryProvider<LibraryImplementation> {

        public final List<LibraryImplementation> libs = new ArrayList<LibraryImplementation>();
        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void set(LibraryImplementation... nue) {
            libs.clear();
            libs.addAll(Arrays.asList(nue));
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
        }

        public LibraryImplementation[] getLibraries() {
            return libs.toArray(new LibraryImplementation[0]);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

    }

    public static class TestLibraryTypeProvider implements LibraryTypeProvider {


        public String getDisplayName() {
            return "j2test";
        }

        public String getLibraryType() {
            return "j2test";
        }

        public String[] getSupportedVolumeTypes() {
            return new String[] {"jars","sources"};
        }

        public LibraryImplementation createLibrary() {
            return LibrariesSupport.createLibraryImplementation("j2test", new String[] {"jars","sources"});
        }

        public void libraryDeleted(LibraryImplementation library) {
        }

        public void libraryCreated(LibraryImplementation library) {
        }

        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public org.openide.util.Lookup getLookup() {
            return null;
        }
    }
    
    private static void registerTestLibraryTypeProvider () throws Exception {
        StringTokenizer tk = new StringTokenizer("org-netbeans-api-project-libraries/LibraryTypeProviders","/");
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        while (tk.hasMoreElements()) {
            String pathElement = tk.nextToken();
            FileObject tmp = root.getFileObject(pathElement);
            if (tmp == null) {
                tmp = root.createFolder(pathElement);
            }
            root = tmp;
        }
        if (root.getChildren().length == 0) {
            InstanceDataObject.create (DataFolder.findFolder(root),"TestLibraryTypeProvider",TestLibraryTypeProvider.class);
        }
    }
}
